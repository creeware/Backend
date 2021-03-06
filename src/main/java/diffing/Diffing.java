package diffing;

import java.io.*;
import java.nio.file.Files;

import static util.Directory.deleteDirectory;

public class Diffing {

    public static String diffRepositories(String userURL, String solutionURL){
        String folder = userURL.split("/")[4].replaceAll(".git", "");
        String[] cmd = {
                "/bin/bash",
                "-c",
                "git clone "
                +userURL
                +"&& cd "
                +folder
                +"&& git remote add -f solution "
                +solutionURL
                +"&& git diff --ignore-space-at-eol -b -w --ignore-blank-lines master solution/master"
        };

        String result = executeCommand(cmd);

        deleteDirectory(new File(folder));

        return result;
    }

    private static String executeCommand(String[] commArr){
        String line;
        String result = "";
        try{
            Process p = Runtime.getRuntime().exec(commArr);
            BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while((line = stdOutReader.readLine()) != null){
               result = result + line + "\n";
            }
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((line = stderrReader.readLine()) != null){
                System.err.println(" .. stderr: "+line);
            }
            int retValue = p.waitFor();
            System.out.println(" .. exit code: "+Integer.toString(retValue));
        } catch(Exception ex){
            System.err.println(ex.toString());
        }
        return  result;
    }
}
