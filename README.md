# MOZRT

SQLite free page recovery tool  version 1.0.

# About this program



# Some Background

SQLite is a software library that implements a simple SQL database engine.
It is very popular and used in different software products.Mozilla Firefox 
and Google Chrome both use SQLite version 3 databases for user data such 
as history, cookies, downloaded files. Google Android and Apple iOS use 
SQLite for many system applications.

A database file might contain one or more pages that are not in active use. 
Unused pages can arise when information is deleted from the database. 
These paged are stored on the free list. They are reused whenever new pages 
are required.    

# Features

The Mozilla Recovery Tool for SQLite allows you to:
 
- recover and browse the content of freelist-pages  
 
- create CSV-format


Some features:

- 100% written with Java standard class library 

- no need for additional archives or JDBC-driver

- runs out of the box


# Licence 

MOZRT for SQLite is bi-licensed under the Mozilla Public License Version 2, 
as well as the GNU General Public License Version 3 or later.

You can modify or redistribute it under the conditions of these licenses. 