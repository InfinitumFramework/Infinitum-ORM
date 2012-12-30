<p align="center">
  <img id="Infinitum Framework" src="http://infinitumframework.com/images/infinitum.jpg" />
</p>

Infinitum ORM
-------------

Infinitum ORM allows developers to spend more time focusing on their problem domain and core business logic and less time on innate data-access and boilerplate code. It embraces object-oriented principles such as polymorphism, inheritance, and association while maintaining a great deal of flexibility. The ORM allows developers to specify what is transient or persistent at a class- and field-level, and it is configurable using either XML mappings or Java annotations. The Infinitum ORM also provides a criteria API for constructing database queries, allowing developers to query on objects rather than tables -- no SQL necessary.

The framework also offers an extensible REST ORM implementation, granting developers an effortless way to communicate with their own RESTful web services using domain objects.

ORM Features
------------

* Non-intrusive: no need to implement or extend any classes
* Automated DDL generation: Infinitum can be configured to automatically create a schema with the necessary tables based on entity classes and relationships
* XML- and annotation- based entity mappings: domain entity classes can be mapped to database tables using either XML map files or Java annotations
* Transparent persistence for Plain Old Java Objects (POJOs)
* Dynamic SQL generation
* Session API: the ORM provides a session interface, which acts as the persistence layer for your application
* Session caching: persistent entities are attached to a session through a session cache, allowing for reduced datastore calls, speedier retrieval, and enforced referential integrity
* Transactional: sessions can be configured to autocommit or transactions can be explicitly committed or rolled back
* Criteria API: build queries using criterion and get domain objects back when you execute them
* Entity associations: specify associations (one-to-many, many-to-one, one-to-one, many-to-many) and let Infinitum handle populating relationships
* Lazy- and eager- loading: Infinitum can be configured to lazily or eagerly load associated collections on-the-fly
* Entity cascading: persistent domain entities can be configured to cascade at the class level, meaning persistent objects associated with an entity being saved will also be saved
* Custom type adapters: register custom type adapters to allow Infinitum's ORM to map any type
* Datastore-agnostic: make calls to SQLite databases or RESTful web services without distinction
