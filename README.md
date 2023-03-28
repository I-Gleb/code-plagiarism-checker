# code-plagiarism-checker

## Indexing mechanism

This part is written on Python and can be found in [index_repo.py](indexing_mechanism/index_repo.py).

### Description 

This script iterate over all source code files in given directory and from each file Token.Names are extracted using [pygments](https://pygments.org/).
On those tokens an inverted index table is build and saved into a given MySQL database in table "tokens".

### Usage

```
Usage: 
    python3 index_repo.py [OPTIONS] <repo_path>
Available options:
    -d,--database <database_name>
    -h,--host <host>
    -u,--user <user>
    -p,--password <password>
    --help 
```

By default, `database_name=mydb`, `host=localhost`, `user=default`, `password=""`.

## Web service

Web part is written using React and Kotlin/JS.

### Description

Simple web application that determines whether a given file is a clone of some file in indexed repository or not. 
An inverted index is taken from a given MySQL database, table "tokens".

On a web page `http://host:port/` there is a from for submiting a file for analysis. 
After submitting on the same page you will either see what file in repo is similar to the one you have submitted, or "OK" if no such file was found.

### Usage

```
Usage: 
    gradle run
Available options:
    -h,--host <host>
    -p,--port <port>
    
    --db-name <database_name>
    --db-host <host>
    --db-user <user>
    --db-password <password>
    --help 
```

By default, `host=localhost`, `port=9090`.