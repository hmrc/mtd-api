API Example Microservice
========================
**Add overview documentation here and use the title of the API and not the URL e.g. "Business details API" and not "business-details-api"**

### Requirements 
- Scala 2.13.x
- Java 8
- sbt 1.6.x
- [Service Manager](https://github.com/hmrc/service-manager)
 
### Development Setup
  
Run from the console using: `sbt run` (starts on port XXXX by default)
  
Start the service manager profile: `sm --start MTDFB_XX`

### Running tests
```
sbt test
sbt it:test
```

### Viewing RAML

To view documentation locally ensure the **add api name** API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:9680/api-documentation/docs/api/preview and use this port and version:
`http://localhost:XXXX/api/conf/1.0/application.raml`

### Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog/wiki)

### Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)


### API Reference / Documentation 
Available on the [Documentation](https://developer.service.hmrc.gov.uk/api-documentation) (find and link the page for specific api)


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")


### Package structure **Delete this section and its sub-sections**

The code is organised into the following packages:
```
app
|__ api
|__ config
|__ definition
|__ routing
|__ utils
|__ v1   (generated) - v1 routes
```
`routing` contains VersionRoutingRequestHandler etc, which provides a form of version fallback for individual API endpoints.
Say if the v1 API has Create, Amend, Delete and Retrieve endpoints, but for v2 only Amend is changing, then only the
Amend endpoint needs a new v2 package. For the others, the request router will locate the next-available (v1) controller.
This replicates the previous behaviour (where everything is copy-pasted to v2) without requiring all the duplicate code.

## api level

All of these packages contain classes common to all versions, except for `endpoints` and `validations`:
```
api
|__ connectors
|__ controllers
|__ endpoints
|__ hateoas
|__ mocks
|__ models
|__ services    - back-end services used by all versions
|__ validations
|__ anyVersion  - common eg JsonValidation, NumberValidation
```

## endpoints

Each package contains business-specific endpoints grouped together, e.g. all the Sample actions are under `sample`.

Beneath either one are the "CRUD" endpoints - create, delete, amend etc:
```
endpoints
  |__ sample      - Sample endpoints
  |__ connector
  |__ domain
```
`connector` and `domain` contain code that can be version-specific, but shared by different endpoints in the same group:
```
sample
  |__ connector
  |__ anyVersion - if any connector code is common for all versions
  |__ v2v3       - if any connector code is common for v2 and v3
  |__ v3         - v3-specific connectors
```
Then in turn, for each CRUD action such as "amend":

```
amend
  |__ anyVersion - if any 'amend' classes are common for all versions
  |__ v1v2       - if any 'amend' classes are common for v1 and v2
  |__ v1
      |__ model
      |__ request
      |__ response
```
The above `v1` package (for example) contains the Controller and Service.

`model` contains any model classes specific to the controller and backend service, e.g. (for amendOrder) `Claim`.

`request` contains the Connector (if there's a specific one), Parsers, Request and Validators.

`response` just contains the `Response` class. It has its own package for symmetry with `request`.


## Decommissioning API versions

Removing an older API version is no longer as simple as deleting the top-level "v2" package - however it should still be straightforward:

1. Find all the `v2` packages beneath `endpoints` (there may be one or two others under, e.g. `models.errors.v2` & `validations.v2`).
2. Where the `v2` has a `v3` equivalent, simply delete `v2`.
3. If there's no `v3` (i.e. the router uses `v2` as a fallback), rename `v2` to `v3` - also update in `v3.routes`.


## Request Router

`VersionRoutingRequestHandler` & associated classes allow for the controlled "version fallback".

There's now a `Version` sealed trait (instead of the previous string type) which defines the version name & config name ("3.0" & "3"),
plus an optional `maybePrevious`.

`VersionRoutingRequestHandler.findRoute(request, version)` then matches the request with the latest "allowed" API version,
handling the fallback via `Version.maybePrevious`.

So if a client requests a version that doesn't exist + no fallback has been defined, they'll still get a "VersionNotFound" response.
