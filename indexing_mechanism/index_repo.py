import os, sys, getopt
from pygments import lex
from pygments.lexers import guess_lexer_for_filename
import mysql.connector

# build reverse index of the repository in given database
def indexDirectory(dirPath : str, db : mysql.connector.connection_cext.CMySQLConnection):    
    mycursor = db.cursor()

    mycursor.execute("DROP TABLE IF EXISTS tokens")
    mycursor.execute("CREATE TABLE tokens (token VARCHAR(255), file VARCHAR(255))")
   
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

            my_lexer = None
            try:
                my_lexer = guess_lexer_for_filename(fullpath, text)
            except:
                pass

            if my_lexer != None:
                name_tokens = filter(lambda t : (str(t[0]) == "Token.Name"), lex(text, my_lexer))
                sql = "INSERT INTO tokens (token, file) VALUES (%s, %s)"
                mycursor.executemany(sql, list(map(lambda t : (t[1], fullpath), name_tokens)))
    db.commit()


def main(argv : list[str]):
    opt_info = "index_repo.py -d <database_name> -h <host> -u <user> -p <password> <repo_path>"

    try:
        opts, args = getopt.getopt(argv,"hd:h:u:p:",["database=","host=","user=","password="])
    except getopt.GetoptError:
        print(opt_info)
        sys.exit(2)

    repo = args[0]
    myDatabase = "mydb"
    myHost = "localhost"
    myUser = "default"
    myPassword = ""
    for opt, arg in opts:
        if opt == '-h':
            print(opt_info)
            sys.exit()
        elif opt in ("-d", "--database"):
            myDatabase = arg
        elif opt in ("-h", "--host"):
            myHost = arg
        elif opt in ("-u", "--user"):
            myUser = arg
        elif opt in ("-p", "--password"):
            myPassword = arg

    mydb = mysql.connector.connect(
        host=myHost,
        user=myUser,
        password=myPassword,
        database=myDatabase
    )

    indexDirectory(repo, mydb)


if __name__ == "__main__":
   main(sys.argv[1:])