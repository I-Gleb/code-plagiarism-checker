import os, sys, getopt
from pygments import lex
from pygments.lexers import guess_lexer
import mysql.connector

# build reverse index of the repository in given database
def indexDirectory(dirPath : str, db : mysql.connector.connection_cext.CMySQLConnection):    
    mycursor = db.cursor()

    mycursor.execute("DROP TABLE IF EXISTS tokens")
    mycursor.execute("CREATE TABLE tokens (token VARCHAR(511), file VARCHAR(511))")
   
    for dirpath, _, filenames in os.walk(dirPath):
        if ".git" in dirpath: continue
        for filename in filenames:
            fullpath = os.path.join(dirpath, filename)
            text = ""
            print(fullpath)
            try:
                file = open(fullpath, "r")
                text = file.read()
                file.close()
            except:
                file.close()    

            my_lexer = guess_lexer(text)
            name_tokens = filter(lambda t : str(t[0]).startswith("Token.Name"), lex(text, my_lexer))
            sql = "INSERT INTO tokens (token, file) VALUES (%s, %s)"
            mycursor.executemany(sql, list(set(map(lambda t : (str(t[0]) + "." + t[1], fullpath), name_tokens))))
            db.commit()


def main(argv : list[str]):
    opt_info = """Usage:
        python3 index_repo.py [OPTIONS] <repo_path>
    Available options:
        --db-name TEXT      Database name
        --db-host TEXT      Database host
        --db-port INT       Database port number
        --db-user TEXT      Database user name
        --db-password TEXT  Database user password
        -h, --help
    """

    try:
        opts, args = getopt.getopt(argv,"h",["help","db-name=","db-host=","db-port=","db-user=","db-password="])
    except getopt.GetoptError:
        print(opt_info)
        sys.exit(2)

    if (len(args) == 0):
        print(opt_info)
        sys.exit(2)

    repo = args[0]
    dbDatabase = "mydb"
    dbHost = "localhost"
    dbPort = 3306
    dbUser = "default"
    dbPassword = ""
    for opt, arg in opts:
        if opt in ("--help", "-h"):
            print(opt_info)
            sys.exit()
        elif opt in ("--db-name"):
            dbDatabase = arg
        elif opt in ("--db-host"):
            dbHost = arg
        elif opt in ("--db-port"):
            dbPort = int(arg)
        elif opt in ("--db-user"):
            dbUser = arg
        elif opt in ("--db-password"):
            dbPassword = arg

    mydb = mysql.connector.connect(
        host=dbHost,
        user=dbUser,
        port=dbPort,
        password=dbPassword,
        database=dbDatabase
    )

    indexDirectory(repo, mydb)


if __name__ == "__main__":
   main(sys.argv[1:])