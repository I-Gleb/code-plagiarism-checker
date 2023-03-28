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
    --db-name TEXT      Database name
    --db-host TEXT      Database host
    --db-port INT       Database port number
    --db-user TEXT      Database user name
    --db-password TEXT  Database user password
    -h, --help 
```

By default, `db-name=mydb`, `db-host=localhost`, `dp-port=3306`, `db-user=default`, `db-password=""`.

## Web service

Web part is written using React and Kotlin/JS.

### Description

Simple web application that determines whether a given file is a clone of some file in indexed repository or not. 
An inverted index is taken from a given MySQL database, table "tokens".

On a web page `http://host:port/` there is a form for submiting a file for analysis. 
After submitting on the same page you will either see what file in repo is similar to the one you have submitted, or "OK" if no such file was found.
Similarity is determined by the condition: "file has at least 85% of Token.Names of the file, that we analyze"
### Usage

```
Usage: 
    gradle run --args="[OPTIONS]"
Options:
    --host TEXT         Host name
    --port INT          Port number
    
    --db-name TEXT      Database name
    --db-host TEXT      Database host
    --db-port INT       Database port number
    --db-user TEXT      Database user name
    --db-password TEXT  Database user password
    -h, --help          Show this message and exit
```

By default, `host=localhost`, `port=9090`, `db-name=mydb`, `db-host=localhost`, `dp-port=3306`, `db-user=default`, `db-password=""`.