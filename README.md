# spring-mongodb-multi-tenancy-example
Example project which demonstrates multi-tenancy support with reactive mongo and spring

This project is just a proof of concept if and how it works.

## Example problem to be solved
We assume we need a service which provides a simple api for products. This service can be configured for multiple customers which 
product data is stored in different databases for security reasons. 

## How it works

The tenants can configured via standard spring ConfigurationProperties (TenantProperties).

To distinguish between the customer I simply added a WebFilter (TenantExtractingWebFilter) which extracts the tenantId from a http header (X-Tenant).
In reality you would get that tenantId from a jwt token for example. 

The filter puts the tenantId in the [Project Reactor's Context](https://projectreactor.io/docs/core/release/reference/#context) to provide it for upstream reactor flow.  
This tenantId can now be accessed in any upstream calls initiated through a http request.

**Now the tricky part:**

ReactiveMongoTemplate simply does not support multi-tenancy. 
It gets only one _ReactiveMongoDatabaseFactory_ for one _MongoClient_ and thus cannot 'route' the queries to different databases. 

I implemented a _MultiTenancyReactiveMongoTemplate_ which takes a _MultiTenancyReactiveMongoDatabaseFactory_ which simply
extends _SimpleReactiveMongoDatabaseFactory_ with the tenantId. Internally the _MultiTenancyReactiveMongoTemplate_ 
creates a _ReactiveMongoTemplate_ for each _MultiTenancyReactiveMongoDatabaseFactory_ and holds them. On each call on any
public api of _MultiTenancyReactiveMongoTemplate_ it internally delegates to the respective _ReactiveMongoTemplate_ 
by retrieving the _tenantId_ from the _subscriberContext_. 

The auto-configuration for this is done by _MultiTenancyConfiguration_ which creates it from the provided _TenantProperites_.

This works for almost all classic interactions of _MongoOperations_ including auto-index creation and usage of _ReactiveMongoRepository_.
Look at the Problems sections for operations which do not work and other limitations.

## Problems and limitations

A few operations as 
 - changeStream (ReactiveChangeStream)
 - indexOps (except the automatic index creation during startup)
 - inTransaction
 - withSession
 
 do not work with this implementation as they would need new delegating implementations or are currently simply not possible as 
 they return scalar values and so there is no possibility to access the _subscriberContext_.

## Run Demo

- clone repo
- run `docker-compose up -d` to spin up a local mongodb instance for simple testing
- run `mvnw spring-boot:run` to start the app
- use the 'insomnia_workspace.json' and import the workspace into [Insomnia](https://insomnia.rest/)
