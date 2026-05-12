# JPA Entity Graph Guide

This document explains what JPA Entity Graphs are, why this project uses them, how they are declared on `OrderEntity`, and how to apply them inside a Panache repository. It also covers common pitfalls and a step-by-step checklist for adding a new graph.

---

## 1. What Is a JPA Entity Graph?

By default, Hibernate maps every `@ManyToOne` and `@OneToMany` association with `FetchType.LAZY`. This is the right default — loading associations you don't need wastes memory. But lazy loading inside a loop creates the **N+1 query problem**:

```
SELECT * FROM Pedido WHERE id_vendedor = ?          -- 1 query

For each order in the list:
    SELECT * FROM Agricultor WHERE id_agricultor = ?  -- N queries
    SELECT * FROM Ubicacion WHERE id_ubicacion = ?    -- N queries
```

One hundred orders → 201 SQL statements. An entity graph tells Hibernate which associations to load **in a single JOIN** at query time, without changing the default `LAZY` setting on the mapping itself.

---

## 2. LOAD vs FETCH Graph Types

Hibernate supports two hint keys:

| Hint key | Behavior |
|---|---|
| `jakarta.persistence.loadgraph` | Loads the specified nodes eagerly; **respects** the existing `FetchType` annotation for unspecified nodes |
| `jakarta.persistence.fetchgraph` | Loads the specified nodes eagerly; **overrides all** unspecified nodes to LAZY |

**Convention in this project: always use `loadgraph`.**

`loadgraph` is safer because it respects any `EAGER` mapping you may have elsewhere and only adds eager loading for the associations you explicitly name. `fetchgraph` can silently make previously-eager associations lazy, causing `LazyInitializationException` if the session is closed before they're accessed.

---

## 3. `@NamedEntityGraph` Anatomy

Entity graphs are declared directly on the entity class with `@NamedEntityGraph`. The annotation takes a unique `name` and a list of `attributeNodes`. Each node can optionally reference a `subgraph` for nested associations.

The worked example from this project is `OrderEntity`:

```java
@Entity
@Table(name = "Pedido")
@NamedEntityGraphs({

    // Graph 1 — avoids N+1 when building the map-coordinate list
    @NamedEntityGraph(
        name = "Pedido.withFarmerLocation",
        attributeNodes = {
            @NamedAttributeNode(value = "farmer", subgraph = "farmer-full"),
            @NamedAttributeNode("seller"),
            @NamedAttributeNode("orderStatus")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "farmer-full",
                attributeNodes = {
                    @NamedAttributeNode("user"),
                    @NamedAttributeNode("location")
                }
            )
        }
    ),

    // Graph 2 — avoids N+1 when rendering the order-detail page
    @NamedEntityGraph(
        name = "Pedido.withDetails",
        attributeNodes = {
            @NamedAttributeNode(value = "details", subgraph = "detail-product"),
            @NamedAttributeNode("farmer"),
            @NamedAttributeNode("seller"),
            @NamedAttributeNode("orderStatus")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "detail-product",
                attributeNodes = { @NamedAttributeNode("product") }
            )
        }
    )
})
public class OrderEntity { ... }
```

| Annotation | Purpose |
|---|---|
| `@NamedEntityGraph(name = "...")` | Registers the graph under a unique string key |
| `@NamedAttributeNode("field")` | Eagerly loads the named Java field (not column) |
| `@NamedAttributeNode(value = "field", subgraph = "key")` | Loads the field AND descends into the named subgraph |
| `@NamedSubgraph(name = "key", attributeNodes = {...})` | Defines which fields of the associated entity are also loaded |

**Key rule:** the values in `@NamedAttributeNode` and `mappedBy` are **Java field names**, not SQL column names.

---

## 4. Applying Graphs in a Panache Repository

Panache's `find()` / `findByIdOptional()` convenience methods have no hook for passing JPA hints. To use a graph you must drop down to `getEntityManager()`, which is available in any `PanacheRepositoryBase` subclass with no extra injection.

```java
// OrderRepositoryImpl.java
@Override
public List<Order> findAllBySellerIdWithFarmerLocation(Long sellerId) {
    EntityGraph<?> graph = getEntityManager().getEntityGraph("Pedido.withFarmerLocation");
    return getEntityManager()
            .createQuery("select o from OrderEntity o where o.sellerId = :sid", OrderEntity.class)
            .setParameter("sid", sellerId)
            .setHint("jakarta.persistence.loadgraph", graph)     // ← the graph is applied here
            .getResultList()
            .stream()
            .map(OrderMapper::toDomain)
            .toList();
}

@Override
public Optional<Order> findByIdWithDetails(Long orderId) {
    EntityGraph<?> graph = getEntityManager().getEntityGraph("Pedido.withDetails");
    TypedQuery<OrderEntity> query = getEntityManager()
            .createQuery("select o from OrderEntity o where o.orderId = :id", OrderEntity.class)
            .setParameter("id", orderId)
            .setHint("jakarta.persistence.loadgraph", graph);
    List<OrderEntity> results = query.getResultList();
    return results.isEmpty()
            ? Optional.empty()
            : Optional.of(OrderMapper.toDomain(results.get(0)));
}
```

Name graph-using methods so callers know a join will happen:
- `findAllBySellerIdWithFarmerLocation` — signals the graph
- `findByIdWithDetails` — signals the graph

Plain queries that don't need graphs keep the standard Panache form:

```java
public List<Order> findAllBySellerId(Long sellerId) {
    return find("sellerId", sellerId).stream().map(OrderMapper::toDomain).toList();
}
```

---

## 5. Named Graphs vs JPQL Fetch Joins

Both approaches eliminate N+1. The choice depends on reuse:

| | Named graph | JPQL fetch join |
|---|---|---|
| **Defined at** | Entity class | Repository method |
| **Reusable** | Yes — any query method can apply the same graph by name | No — the JOIN clause must be repeated |
| **Multiple join shapes** | Multiple `@NamedEntityGraph` definitions | Multiple JPQL queries |
| **Best for** | Fetch shapes reused by several methods | One-off or complex queries |

`VigilanciaFitosanitariaRepositoryImpl` uses JPQL fetch joins for its ad-hoc queries. The order module uses named graphs because the same join shape (farmer + location) is needed by both the map endpoint and any future order-list endpoint.

---

## 6. Before and After — SQL Comparison

**Without graph (N+1):**

```sql
-- GET /api/orders/farmer-locations with 5 orders
SELECT * FROM Pedido WHERE id_vendedor = 1
SELECT * FROM Agricultor WHERE id_agricultor = 1
SELECT * FROM Ubicacion WHERE id_ubicacion = 1
SELECT * FROM Agricultor WHERE id_agricultor = 2
SELECT * FROM Ubicacion WHERE id_ubicacion = 2
SELECT * FROM Agricultor WHERE id_agricultor = 3
SELECT * FROM Ubicacion WHERE id_ubicacion = 3
-- ... 11 queries total for 5 orders
```

**With `Pedido.withFarmerLocation` graph (1 query):**

```sql
SELECT p.*, a.*, u.*, tv.*, ep.*
FROM Pedido p
JOIN Agricultor a ON a.id_agricultor = p.id_agricultor
JOIN Usuario ua ON ua.id_usuario = a.id_usuario
JOIN Ubicacion u ON u.id_ubicacion = a.id_ubicacion
JOIN Tecnico_Vendedor tv ON tv.id_tecnico_vendedor = p.id_vendedor
JOIN Estados_Pedido ep ON ep.id_estado_pedido = p.id_estado_pedido
WHERE p.id_vendedor = 1
```

To observe this in practice, enable SQL logging in `application.properties`:

```properties
quarkus.hibernate-orm.log.sql=true
```

---

## 7. `@OneToMany` Caution — `MultipleBagFetchException`

Hibernate throws `MultipleBagFetchException` when a single query tries to JOIN-fetch **two or more unbounded `@OneToMany` collections simultaneously**. This is a Hibernate constraint, not a SQL limitation.

**Rule: include at most one `@OneToMany` node in any single named graph.**

`Pedido.withDetails` loads `details` (one `@OneToMany`) — safe.  
If you were to add a second `@OneToMany` (e.g. `payments`) to the same graph, Hibernate would throw at startup. The fix is to split into two graphs applied sequentially, or to change one collection to a `@ManyToMany` with a join table.

---

## 8. Checklist for Adding a New Graph

1. **Decide the fetch shape** — draw the object tree you need. Identify which associations are `@ManyToOne` / `@OneToMany` and check whether any existing graph already covers the shape.
2. **Annotate the entity** — add `@NamedEntityGraph` to the owning entity. Use a `"TableName.descriptiveName"` naming convention (e.g. `"Pedido.withFarmerLocation"`).
3. **Verify the Java field names** — confirm that the values in `@NamedAttributeNode` exactly match the Java field names in the entity class, not the SQL column names.
4. **Implement the repository method** — use `getEntityManager().getEntityGraph("...")` + JPQL `TypedQuery` + `setHint("jakarta.persistence.loadgraph", graph)`. Name the method to communicate the join (e.g. `findBy...With...`).
5. **Enable SQL logging** — set `quarkus.hibernate-orm.log.sql=true` and call the endpoint. Confirm that a single SQL JOIN is issued, not N+1 selects.
6. **Write a use-case integration test** — seed the data in `@BeforeEach @Transactional`, call the use case, and assert that the nested fields (e.g. `farmerName`, `latitude`) are populated correctly. This is the only way to verify the graph fires end-to-end.
7. **Disable SQL logging** — remove or set `false` before committing.
