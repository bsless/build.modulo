# io.github.bsless/build.modulo

Modular Clojure projects with tools.deps and tools.build.

Status: experimental

## Usage

Add as a dependency to your build alias:

```clojure
{io.github.bsless/build.modulo {}}
```

In your build program:

```clojure
(require '[bsless.build.modulo :as m])
```

Create a top level `deps.edn`:

```clojure
{:paths ["src" "resources" "module-a/src"]
 :deps {org.clojure/clojure {:mvn/version "1.10.3"}}}
```

Create modules in different sub directories with `deps.edn` files.

Specify which fields should be inherited from the parent specification:

```clojure
{:paths ["src"]
 :deps {org.clojure/clojure {:build.modulo/inherit :all}}}
```

Write build programs which will be executed in a module context:

```clojure
(m/modo [opts {::m/parent "../deps.edn"
               ::m/project-root "./module-foo"
               ::m/pom "../pom.xml"
               ::m/lib 'foo.bar/bazz
               ::m/project "./deps.edn"}]
        (-> opts
            m/unqualify
            (assoc :basis b/create-basis)))
```

Qualified keys will be unqualified by `m/unqualify` and assoc-ed back
into `opts` where they can be used like typical tools.build api keys, with one important difference:

Inside the context of `m/modo` (and `m/module-call`), the parent and
project deps will already be merged.

All specified paths are relative to `::m/project-root`.

### Depending on a specific module

#### As Maven artifact

Just specify the maven coordinates

#### As git dependency

Also for inter module git dependency, use `:deps/root` for a path to the
specific module in the repository

```clojure
{com.acme/foo.bar {:git/tag "" :git/sha "" 
                   :git/url "https://github.com/acme/foo.git"
                   :deps/root "./module-bar"}}
```

When specifying a `:git/url` the dep key can essentially be anything,
that way two modules from the same repository can be depended on without
collision.

To make the skeleton deps usable as a git dependency, you'll need to
annotate it with their current versions, using `m/spit-project` withing
a `modo` context:

```clojure
(m/modo [opts {::m/parent "../deps.edn"
               ::m/project-root "./module-foo"
               ::m/pom "../pom.xml"
               ::m/lib 'foo.bar/bazz
               ::m/project "./deps.edn"}]
        (-> opts
            m/unqualify
            m/spit-project))
```


## Prior work

- [Maestro](https://github.com/helins/maestro.clj) 
- [deps-modules](https://github.com/exoscale/deps-modules) 

## License

Copyright © 2022 Ben Sless

Distributed under the Eclipse Public License version 1.0.
