# Framed
## Intro
Framed is meant as an easy photo sharing application that targets the different parts of a photographers workflow.


## Technical
### Build instructions
The project uses the [simple build tool (SBT)](https://www.scala-sbt.org) with [sbt native docker](https://www.scala-sbt.org/sbt-native-packager/formats/docker.html#) plugin.
This permits to build launch the server as follows:

 - `sbt publishLocal`
 - `docker-compose up`

The latter command starts the database server required as backend to the web server.

### DataBase
The database is integrated into Play with [Slick](http://slick.lightbend.com/doc/2.1.0/orm-to-slick.html) and 
[Play-Slick](https://www.playframework.com/documentation/2.6.x/PlaySlick).

### Heroku deployment

 - `heroku container:login`
 - `docker tag framed-server:1.0-SNAPSHOT registry.heroku.com/framed/web`
 - `docker push  registry.heroku.com/framed/web`
 
See [heroku Docker deployment](https://devcenter.heroku.com/articles/container-registry-and-runtime) for further 
instructions.