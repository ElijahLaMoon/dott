# Dott
This project is an exercise application to gather some statistics on fictional e-commerce platform

### Note
When I was assigned this exercise I was suggested to take 2 approaches: a straightforward one, fast and dirty, and an "overengineered" one.
Since recently I have developed and deployed [some personal application](https://github.com/ElijahLaMoon/ukrnastup-comments) it was natural to use it as my guideline.
Thus, I decided to build this application starting with a database (SQLite + Quill), and it quickly became apparent this is going to be the "overengineered" solution.
However, soon enough (to be able to implement basic required functionality) I realized it is going to take way more time than I was allowed, so I pivoted to the "simple" approach.
Thankfully, after that I was granted with some more time, so I actually did have a chance to complete my overengineered solution.
All that said, I encourage you to explore the `overengineered` folder at the root of this project first, as it showcases my passion for Scala and knowledge acquired througout the years, and I am reasonably happy with this result.

## Overview
As I explained in the [note above](#note), this is a multi-project build, where each sub-project is just a different way to approach the task at hand.
The task, by the way, goes as follows:

### Task
The purpose of this exercise is to check if older products are still being sold in Dott. Given a database with a schema like this:
* `Product`: information about the product (name, category, weight, price, creation date, product UUID)
* `Item`: wrapper with additional information about the purchased product (shipping fee, tax amount, total cost incuding profit)
* `Order`: contains general information about the order (customer name and contact, shipping address, grand total, date when the order was placed, order UUID)

... the project provides the ability to filter only the items which were added to the database between specified timestamps and count the amount of orders in the last N months that contain those filtered items.
So, if you specify some fairly distant timeframe and there's a recent order — then yes, it means older products are still being sold on the Dott platform.

### Technical stack
1. SQLite as embedded database
2. [Quill](https://zio.dev/zio-quill/) as database access layer with compile-time generated queries
3. [Cats Effect](https://typelevel.org/cats-effect/) as IO Monad
4. [FS2](https://fs2.io/) as functional streams
5. [Decline](https://ben.kirw.in/decline/usage.html) as command-line interface
6. [Doobie](https://tpolecat.github.io/doobie/) with [Quill integration](https://zio.dev/zio-quill/contexts/#quill-doobie) as database actions transactor

### Problems and ways to resolve
- First of all, the `overengineered` solution has no tests whatsoever (those in "simple" don't even count).
When I was developing it I just created a worksheet and by using a magic of common sense and `println`s I was guiding myself towards correct functionality of the app's modules.
That said, unlike `simple` this sub-project actually is modular enough to be suitable for proper testing.
I think this is a solid and interesting case for learning property-based testing with [ScalaCheck](https://index.scala-lang.org/typelevel/scalacheck)
- I have a strong feeling there's still a decent space for optimizations, mainly of database access in `Database.scala` and `IntervalsFilters.scala`. I already spent a decent chunk of time optimizing some queries which led to improved times of insertions, from 22s to insert 2k generated orders and ~6k rows of relations between them and items to 32s to insert 200k orders and ~600k relationships (on my laptop's Ryzen 7 5800H), but still
- Not particularly a problem, but when you first open the code you'll see a error about `buildinfo` import. This is a normal behaviour, just open `sbt` shell and run `compile`
- I don't know how to resolve it, but if you're going to test out the application via running jar instead of `sbt run` then remember that there have to be no spaces between optional parameters and their values, i.e. `-p5000 -i7-14 -i>18`
- To populate a database you still need to provide timestamps, that should be redefined as [mutually exclusive options](https://ben.kirw.in/decline/usage.html#combining-options)

## How to run
This application relies on a local database and it provides the ability to fill a database with randomly generated data.
However note, that trying to populate a database which already was populated will result in a runtime crash.
This action is meant to be run only once per database.

### Interface
Run `sbt run --help` to see this
```
Usage: java -jar dott.jar [--populate <integer>] [--interval <custom interval>]... <starting timestamp> <closing timestamp>


A utility to check whether older products are still being sold in Dott.
Provide 2 timestamps of type YYYY-MM-DD to filter items created between them.
Optionally, provide a time interval or several to limit/expand search of orders which contain those filtered items


Options and flags:
    --help
        Display this help text.
    --version, -v
        Print the version number and exit.
    --populate <integer>, -p <integer>
        Populate database with randomly generated data by specifying a number of rows to generate.
        Please note running this option on an already populated database will most likely fail
        or lead to incorrect results
    --interval <custom interval>, -i <custom interval>

        Provide a custom interval to filter orders, in months. Can filter by:
          1. In-between interval, e.g. '2-7' filters orders placed between 2 and 7 months ago
            1.1. Please note than bounds are inclusive and left-hand side has to be lesser than right one
          2. Newer than interval, e.g. '<3' filters orders that were places in the last 3 months
          3. Older than interval, e.g. '>8' filters all orders that were placed more than 8 months ago
        To provide multiple intervals just pass them as several options with their own flags, e.g. '-i 4-5 -i <3 -i >15'
```

### Initial setup
```
$ sbt
> compile; run 2018-03-17 2024-01-01 -p 5000
```
This will generate and insert ~11 products/items, 5000 orders, and approximately 15000 order-item relationships

### Run examples
Assuming you have a populated database
```
$ sbt
> run 2018-03-17 2024-01-01
...
----------------------------------------
Products with creation date between 2018-03-17 and 2024-01-01:
Item(Product(ProductName(iPhone 8),Electronics,0.2 kg,€399,ProductCreationDate(2019-09-29T00:00),ProductUuid(d374c83b-b505-4072-b7e8-8bfe44072e7b)),€20,€71.82,€550.67);
Item(Product(ProductName(iPhone X),Electronics,0.3 kg,€599,ProductCreationDate(2021-01-14T00:00),ProductUuid(798adf13-d3ad-4a30-8658-d256bc3bf7ea)),€20,€107.82,€816.67);
Item(Product(ProductName(Google Pixel 6),Electronics,0.3 kg,€499,ProductCreationDate(2022-02-13T00:00),ProductUuid(a24e160d-c64a-4d6c-9f56-4fcda7023457)),€20,€89.82,€683.67);
Item(Product(ProductName(Lenovo Legion 15ACH6H 2022),Electronics,2.0 kg,€1499,ProductCreationDate(2021-06-28T00:00),ProductUuid(988428be-5de7-460d-a759-a17db90b0aae)),€20,€269.82,€2013.67);
Item(Product(ProductName(Martin Odersky, Programming in Scala 5th edition),Books,0.512 kg,€19.99,ProductCreationDate(2023-04-16T00:00),ProductUuid(628ade0b-3c40-4e13-bbaa-b7cfcea0bf9d)),€10,€1.4,€34.39);
Item(Product(ProductName(Bartosz Milewski, Category Theory for Programmers),Books,0.354 kg,€14.99,ProductCreationDate(2022-05-17T00:00),ProductUuid(5578b73e-4ab0-40a7-9ea1-bde601c58a5d)),€10,€1.05,€28.29)
----------------------------------------
Orders placed in timeframes:
1-3 months: 0 orders
4-6 months: 0 orders
7-12 months: 114 orders
>12 months: 1749 orders
----------------------------------------
```

```
$ sbt
> run 2015-02-24 2020-10-05 -i 3-9 -i >18
...
----------------------------------------
Products with creation date between 2015-02-24 and 2020-10-05:
Item(Product(ProductName(iPhone 8),Electronics,0.2 kg,€399,ProductCreationDate(2019-09-29T00:00),ProductUuid(d374c83b-b505-4072-b7e8-8bfe44072e7b)),€20,€71.82,€550.67);
Item(Product(ProductName(Simon L. Peyton Jones, The Implementation of Functional Programming Languages),Books,0.402 kg,€16.49,ProductCreationDate(2015-04-24T00:00),ProductUuid(3ecb7fd2-bf3f-4e7b-a26f-21baeee38c41)),€10,€1.15,€30.12);
Item(Product(ProductName(Edwin Brady, Type-Driven Development with Idris),Books,0.318 kg,€17.49,ProductCreationDate(2017-09-20T00:00),ProductUuid(57cfeff0-4a94-4a1e-b6bc-02250dc95877)),€10,€1.22,€31.34)
----------------------------------------
Orders placed in custom timeframe 3-9: 0 orders
Orders placed in custom timeframe >18: 3433 orders
----------------------------------------
```