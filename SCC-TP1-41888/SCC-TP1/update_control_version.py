from datetime import datetime


def to_str(dt_time):
    return str(dt_time.day) + str(dt_time.hour) + str(dt_time.minute)


def main():
    # put file path here
    filepath="/Users/admin/MIEI/scc/pra/SCC-TP1/src/scc/utils/Configurations.java"

    #open file in read mode
    file = open(filepath, "r")
    replaced_content = ""

    #looping through the file
    for line in file:

        #stripping line break
        line = line[0:-1]

        if "CONTROL_VERSION" in line:
            #replacing the texts
            control =  to_str(datetime.now())
            print("version: " + control)
            new_line = line.split("=")[0] + "=\"" + control + "\";"
        else:
            new_line = line

        #concatenate the new string and add an end-line break
        replaced_content = replaced_content + new_line + "\n"

        
    #close the file
    file.close()

    #Open file in write mode
    write_file = open(filepath, "w")

    #overwriting the old file contents with the new/replaced content
    write_file.write(replaced_content)

    #close the file
    write_file.close()


if __name__ == "__main__":
    main()