import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by piyush on 3/25/2017.
 */

public class main {


    public static void main(String[] args) throws IOException {

        Path currentRelativePath = Paths.get("");
        String current_path = currentRelativePath.toAbsolutePath().toString();
        String[] zipfile;
        String filename;
        zipfile = args[0].split("\\\\");
        filename=zipfile[zipfile.length-1];
        if (args.length==0) {
            System.out.println("file name missing");
            return;
        }

        filename=filename.replace(".zip","");
        String DestDirectory=current_path+"\\"+filename;
        unziputility(args[0],DestDirectory);
        String JARDestDirectory=DestDirectory;
        String jarfilename=DestDirectory+"\\"+filename+".jar";
        unjarutility(jarfilename,JARDestDirectory);

        if(args.length>1)
            PreDeploymentCheck(JARDestDirectory+"\\webapp",args[1]);
        else
            PreDeploymentCheck(JARDestDirectory+"\\webapp","null");

        File fp= new File(DestDirectory);
        boolean deleteproc=deleteDir(fp);
        if(deleteproc)
            System.out.println("deleted files");
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir (new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static void unziputility(String filename,String destdirectory) {
            String zipFileName = filename;
            String destDirectory = destdirectory;
            UnzipUtility unzipper = new UnzipUtility();
            try {
                unzipper.unzip(zipFileName, destDirectory);
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }

    public static void PreDeploymentCheck(String current_path,String conf_file_path)
    {
        String output=null;

        try{

            //   For BMC bundle source code analysis
            //   String path = current_path+"\\bundle\\src\\main\\webapp";
            boolean lintfreeflag = false;
            String command=null;
            if(conf_file_path=="null")
                command = "cmd /c eslint "+current_path+"/**";
            else
                command="cmd /c eslint -c "+conf_file_path+" "+current_path+"/**";

            Process child = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()));
            StringBuilder out = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                out.append(line+"\n");
            }
            reader.close();

             output=out.toString();
            String[] outputArray = output.split("\n");
            String summary=outputArray[outputArray.length-1];

            if(summary.length()!=0) {
                String[] summaryArray = summary.split(" ");
                String temp=summaryArray[3].replace("(","");
                if(temp=="0")
                    lintfreeflag=true;
            }
            else{
                //continue deployment
                lintfreeflag=true;
            }
            if(lintfreeflag)
                System.out.print("file lints completed\ncontinuing with deployment");
            else {
                System.out.println("deployment cancelled");
                System.out.println("List of Errors :");
                System.out.println(output);
            }

        } catch (IOException e) {
            System.out.println("Ioexception occured"+e.toString());
        }
        logfilecreation(output);
    }

    public static void logfilecreation(String output){

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
//            Strin output;

            File file = new File("logs.txt");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            bw.write(output);

            System.out.println("Logs.txt generated");

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {
                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void unjarutility(String jarFile,String destdir) throws IOException {

        try {
            String initcommand= "cd "+destdir+" ";
//            Process child = Runtime.getRuntime().exec(initcommand);
            String command = initcommand+" && "+" jar xf "+jarFile;

//            child = Runtime.getRuntime().exec(command);

            Process child=Runtime.getRuntime().exec("cmd /c \""+command+"\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line+"\n");

            }

            System.out.println(out.toString());   //Prints the string content read from input stream
            reader.close();

        } catch (IOException e) {
        }




    }

}


