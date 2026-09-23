Prerequisites:
1. Vault
2. Postgres db
3. Kafka server
4. To connect to the vault service provider class is used and vault instance should allow that

Installation:
1. Update values under deployment.db/kafka/vault/urls
2. To deploy consumer deployment.mode should be CONSUMER, and deployment.vault.hashicorp.role should be "contract-consumer".
   To deploy provider deployment.mode should be PROVIDER, and deployment.vault.hashicorp.role should be "contract-provider".

Image selection:

`deployment.image.artifact` defaults to the image published for this fork, and
`deployment.image.tag` defaults to the chart's `appVersion`, so a plain `helm install` deploys the
released version. Override either one to deploy a different build:

```
helm install contract ./charts \
  --set deployment.image.artifact=ghcr.io/<owner>/contract \
  --set deployment.image.tag=<commit-sha>
```

The pipeline tags every image with both the commit SHA and the project version, so either value
works as a tag.

While the image is private, the cluster needs credentials to pull it. Create the secret in the
target namespace and reference it:

```
kubectl create secret docker-registry ghcr-creds \
  --docker-server=ghcr.io --docker-username=<user> --docker-password=<token>

helm install contract ./charts --set deployment.image.pullSecrets[0].name=ghcr-creds
```
