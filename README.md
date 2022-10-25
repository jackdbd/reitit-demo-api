# Reitit demo API

A simple API to learn [Reitit](https://github.com/metosin/reitit).

## Installation

This project can be managed with the [Babashka Tasks Runner](https://book.babashka.org/#tasks). The tasks are defined in [this bb.edn file](./bb.edn). You can check the list of available tasks using this command:

```sh
bb tasks
```

Install all npm packages and all jars:

```sh
bb install
```

## Development

I use VS Code and [Calva](https://calva.io/) to write Clojure, and [neil](https://github.com/babashka/neil) to manage the `deps.edn` and `build.clj` files.

To start a project REPL and start developing, open VS Code and do the following:

1. Type `ctrl+alt+c` `ctrl+alt+j`: start a project REPL and connect (aka [jack-in](https://calva.io/connect/)) with Calva.
1. Select `deps.edn` as project type.
1. Select `:dev` as the alias.

The instructions above execute the code found in the [user](./dev/user.clj) namespace.

In the `user.clj` file there are also several [rich comments](https://practical.li/clojure/clojure-cli/projects/rich-comments.html) to start/stop the stateful component of the app (managed by [integrant](https://github.com/weavejester/integrant)), and to launch [Portal](https://github.com/djblue/portal) and send [taps](https://clojuredocs.org/clojure.core/tap%3E) to it.

You can type `ctrl+alt+space space` to see the list of [custom REPL commands](https://calva.io/custom-commands/) you can run (TODO: ensure that the port used to communicate with Portal is correct). These commands and their shortcuts are defined in the `:customREPLCommandSnippets` key in [.calva/config.edn](.calva/config.edn). For example, `ctrl+alt+space p` launches [portal](https://github.com/djblue/portal).

*Tip*: for a more complete setup for VS Code and Calva, see Sean Corfield's [vscode-calva-setup](https://github.com/seancorfield/vscode-calva-setup) repo.

Run the app:

```sh
bb serve
```

Make some requests:

```sh
curl -X GET "http://localhost:$PORT/math/plus?x=2&y=3" -i
```

```sh
curl -X POST "http://localhost:$PORT/math/plus" \
  -H 'Content-Type: application/json' \
  -i \
  -d '{ "x": 2, "y": 3 }'
```

Launch PostgreSQL using Docker Compose:

```sh
bb dc-up
```

When you are done, stop all containers using this command:

```sh
bb dc-down
```

## Database migrations

I prefer writing migrations in SQL, and at the same time using the [Knex CLI](https://knexjs.org/guide/migrations.html) to manage the migration scripts.

Create a new migration script:

```sh
bb migrate:make my-new-migration
```

Write the up/down migrations in SQL.

Then, run all migrations:

```sh
bb migrate:latest
```

Seed the database tables with some records, using a SQL script:

```sh
bb seed
```

In alternative, seed the database tables with some records, using the [snaplet](https://www.snaplet.dev/) CLI:

```sh
bb snaplet-seed
```

Rollback the migration:

```sh
bb migrate:rollback
```

## Test

```sh
clojure -X:test
```

## Version management

This project uses neil to manage versioning.

First, set the project version. These commands set the  in `deps.edn`

```sh
neil version set 0.0.1
neil version major
neil version minor
neil version patch
```

## Deploy (TODO)

Deploy to Google Cloud Run.

```sh
bb deploy
```
