# CI/CD Pipeline (Cloud Build + Cloud Run)

This repository uses Google Cloud Build for CI/CD and deploys Docker images to Cloud Run.

## Branch and Version Flow

1. Feature branches are created from `develop`.
2. Pull requests merge into `develop` after review.
3. `main` is the production branch.
4. Versioned tags (for example `v1.2.0`) represent release candidates promoted to production.
5. Cloud Build computes the image version with this precedence:
	- `TAG_NAME` when triggered by a Git tag.
	- `SHORT_SHA` for branch pushes.
	- `dev` for local/manual runs without Git context.

## Pipeline File

- Main pipeline: `cloudbuild.yaml`.
- Runtime image definition: `Dockerfile`.

## Continuous Integration (Cloud Build)

The CI stage is the `package` step in `cloudbuild.yaml`:

1. Uses Maven + Temurin 17 container.
2. Executes `./mvnw -B -DskipTests package` after tests pass.
3. Produces `target/quarkus-app` required for runtime-only Docker image creation.

## Continuous Delivery (Artifact Registry + Cloud Run)

Delivery stages in `cloudbuild.yaml`:

1. Build Docker image from `Dockerfile` using `APP_VERSION`.
2. Push image tags to Artifact Registry:
	- `${_REGION}-docker.pkg.dev/${_PROJECT_ID}/${_REPOSITORY}/${_IMAGE}:<version>`
	- `${_REGION}-docker.pkg.dev/${_PROJECT_ID}/${_REPOSITORY}/${_IMAGE}:latest`
3. Deploy selected image tag to Cloud Run service `${_SERVICE}`.

## Required Substitution Values

Adjust these values in `cloudbuild.yaml` for each environment:

- `_REGION`: GCP region (for example `us-central1`).
- `_PROJECT_ID`: Google Cloud project ID.
- `_REPOSITORY`: Artifact Registry repository name.
- `_IMAGE`: image name.
- `_SERVICE`: Cloud Run service name.

## Trigger Recommendations

Create Cloud Build triggers for:

1. Push to `develop`: run build/push for integration validation.
2. Push to `main`: run build/push/deploy to production.
3. Tag pattern `v*`: run release deployment.

## Application Environment Variables

The Quarkus backend requires the following environment variables to be set in the Cloud Run service. These are configured separately from Cloud Build substitutions.

### Required Variables

| Variable | Description | Example |
|---|---|---|
| `QUARKUS_DATASOURCE_USERNAME` | MySQL database username | `plaguie_user` |
| `QUARKUS_DATASOURCE_PASSWORD` | MySQL database password | `secure_password_here` |
| `QUARKUS_DATASOURCE_JDBC_URL` | MySQL JDBC connection URL | `jdbc:mysql://db.example.com:3306/plaguie_db` |
| `FIREBASE_SERVICE_ACCOUNT_LOCATION` | Path to Firebase service account JSON inside the container | `/etc/firebase/service-account.json` |

### How to Set Environment Variables in Cloud Run

In the Cloud Build deploy step or Cloud Run service configuration:

```bash
gcloud run deploy back-plaguie-api \
  --image "us-central1-docker.pkg.dev/PROJECT_ID/back-plaguie/back-plaguie-api:latest" \
  --set-env-vars \
    QUARKUS_DATASOURCE_USERNAME=plaguie_user,\
    QUARKUS_DATASOURCE_PASSWORD=your_db_password,\
    QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://db-host:3306/plaguie_db,\
    FIREBASE_SERVICE_ACCOUNT_LOCATION=/etc/firebase/service-account.json
```

Or configure them in Cloud Run Console > Service > Edit > Variables and Secrets.

### Firebase Service Account Setup

The `FIREBASE_SERVICE_ACCOUNT_LOCATION` must point to a file accessible inside the container. Two approaches:

1. **Mount as a secret** (recommended): Use Cloud Run Secrets to inject the Firebase JSON as a mounted file.
2. **Build into image**: Copy the Firebase JSON into the Dockerfile (less flexible for credential rotation).

## Governance Rules

- Releases must be traceable through tags and image versions.
- Avoid direct production changes outside reviewed PRs.
- When branch strategy or deployment targets change, update both `cloudbuild.yaml` and this document.
- Store sensitive values (database password, Firebase key) in Cloud Run Secrets, not in `cloudbuild.yaml`.