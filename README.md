# splitsilly
A course project for Contemporary Java Development course in SU-FMI

## Description
This is the course project for a course tought at Sofia University. Task is to create a console application mimiking the functionality of [SplitWise](https://www.splitwise.com)
We need a server and a client applications communication trought sockets.

## Personal goal
Apart from completing the task at hand I wanted to try to implement the `ActiveRecord` pattern in Java which I managed to some extend. The package is located [here](https://github.com/valeksiev/splitwise/tree/master/src/bg/sofia/uni/fmi/mjt/splitwise/server/activerecord).
- `Base` model adds the finders/persistanse layer methods to the concrete model
- `Persister` is an interface which ensures the Base model uses to connect to the presistance layer
- `FileSystemPersister` is a concrete implementation of the above interface using file system and json files as storage; the arhitecture allows a `MysqlSystemPersister` to be added for example which would use MySQL as storage.

## Personal goal
Because of the above I intentionaly neglected some other parts of the application which could be implemented better - command pattern in the server instead of `if-elseif` for example
