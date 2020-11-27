import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;



public class DriverXtreme {



    // Change these lines if needed
    static final long SEED = 120411;   // Seed value, changing seed value creates different inputs
    static int assign_num = 3;   // Change according to which assignment you want to evaluate
    static final int[] test_cases_lengths = {250,600,1500}; // Length of test cases (Keep the order same for comparing different outputs)
    static final String[] test_cases_names = {"small","medium","large"}; // Name of test case generated
    static boolean generateReport = false;  // Useful if your output doesn't match. Useful details of object is stored, which 
                                                  // can be checked using accompanying jupyter notebook

    // Comment the next line if you don't want to evaluate
    static final String[] correct_outputs = {"pranjal_output_small_120411.out","pranjal_output_medium_120411.out","pranjal_output_large_120411.out"};
    // static final String[] correct_outputs = {};


    static final String root_path = ".";
    
    static boolean largeTestCases = false;
   
   
   


   
    static String name;
    static String output_folder;
    static final int PRIME = 1000003;   // 1 Million + 3

    static List<Double> time_taken;
    static List<Integer> total_ops;
    static MultithreadingDemo loopThread;

    public static void main(String[] args) throws IOException {

        ArgParser argParser = new ArgParser(largeTestCases,assign_num,generateReport);

        argParser.parseArguments(args);
        largeTestCases = argParser.largeTestCase;
        assign_num = argParser.assign_num;
        generateReport = argParser.generateReport;


        // GENERATE TEST CASES
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your first name");
        name = sc.nextLine();
        output_folder = name + "_" + "assign" + assign_num+"_output";


        TestCaseGenerator testCaseGenerator = new TestCaseGenerator(SEED,name,root_path,assign_num,output_folder,PRIME);
        String[] file_names = new String[test_cases_lengths.length];
        for(int i=0;i<file_names.length;i++){
            if(test_cases_lengths[i]>=1000 && !largeTestCases){
                continue;
            }
            file_names[i] = testCaseGenerator.generateTestCases(test_cases_lengths[i],test_cases_names[i]);
        }
        System.out.println("Test Cases Generated Successfully");
        OutputObject[] outputObjs = new OutputObject[file_names.length];
        for(int i=0;i<file_names.length;i++){
            if(test_cases_lengths[i]>=1000 && !largeTestCases){
                continue;
            }
            outputObjs[i] =  testInputs(file_names[i]+".in",output_folder+"/"+name+"_output"+"_"+test_cases_names[i]+"_"+SEED,generateReport);
            resetTimeTaken();
        }

        CodeEvaluater codeEvaluater = new CodeEvaluater();
        for(int i=0;i<correct_outputs.length;i++){
            if(test_cases_lengths[i]>=1000 && !largeTestCases){
                continue;
            }
            boolean temp = codeEvaluater.evaluateCode(root_path + "/" + output_folder+"/"+name+"_output"+"_"+test_cases_names[i]+"_"+SEED+".out",root_path+"/"+correct_outputs[i]);
            if(temp){
                System.out.println("Output of "+test_cases_names[i]+" has matched");
            }else{
                System.out.println("Output of "+test_cases_names[i]+" doesnt match");
                System.out.println("-----------------------");
                System.out.println("Loading first point of difference. Please Wait...");
                getFirstPointOfDifference(i, outputObjs[i]);
            }
        }

    }

    static boolean command_are_equal(Command a,Command b){
        boolean first_por = a.type.equals(b.type);
        if(a.val!=null && b.val!=null){
            first_por = first_por && a.val.intValue() ==b.val.intValue();
        }
        if(a.output!=null && b.output!=null){
            first_por = first_por && a.output.intValue() ==b.output.intValue();
        }
        if(a.corr!=null && b.corr!=null){
            first_por = first_por && a.corr.intValue()==b.corr.intValue() && a.corr_addr.intValue() == b.corr_addr.intValue(); 
        }

        if(a.allocSize!=null && b.allocSize!=null){
            first_por = first_por && a.allocSize.intValue() == b.allocSize.intValue() && a.freeSize.intValue()==b.freeSize.intValue(); 
        }

        return first_por;
    }

    static void getFirstPointOfDifference(int idx,OutputObject orig){
        String path = root_path+"/outputs/"+correct_outputs[idx].replace(".out", ".ser");
        OutputObject corr = loadObject(path);

        if(corr.num_test!=orig.num_test || orig.num_test!=orig.testCases.size() || corr.num_test!=corr.testCases.size()){
            System.out.println("Looks like number of test cases in files don't match. Make sure you are checking against correct input file and try again.");
            return;
        }
        int t = corr.num_test;
        for(int i=0;i<t;i++){
            TestCase testCaseOrig = orig.testCases.get(i);
            TestCase testCaseCorr = corr.testCases.get(i);
            

            if(testCaseCorr.n_commands!=testCaseOrig.n_commands || testCaseCorr.n_commands!=testCaseCorr.commands.size() || testCaseOrig.n_commands!=testCaseOrig.commands.size()){
                System.out.println("Looks like number of commands in test case"+(i+1)+" in files don't match. Make sure you are checking against correct input file and try again.");
                return;
            }
            int n_comm = testCaseCorr.n_commands;
            for(int j=0;j<n_comm;j++){
                Command commOrig = testCaseOrig.commands.get(j);
                Command commCorr = testCaseCorr.commands.get(j);
                if(!command_are_equal(commOrig, commCorr)){
                    System.out.println("The first point of difference is in Test Case: "+(i+1)+" Comm. numb: "+(j+1));
                    if(!commCorr.type.equals(commOrig.type) || !commCorr.val.equals(commOrig.val)){
                        System.out.println(commOrig.type+" "+commCorr.type);
                        System.out.println(commOrig.val.equals(commCorr.val));
                        System.out.println("Commands executed are different. Possible issue in checker file");
                        return;
                    }
                    System.out.println("Command executed:- "+commCorr.type+" "+commCorr.val);
                    if(commCorr.type=="Free"){
                        if(commCorr.corr_addr == commOrig.corr_addr){
                            System.out.println("Which corresponds to freeing address "+commCorr.corr_addr);
                        }else{
                            System.out.println("Which corresponds to freeing address "+commOrig.corr_addr+" in your case");
                            System.out.println("Which corresponds to freeing address "+commCorr.corr_addr+" in proposed correct output");
                            System.out.println("This happens because of difference in sizes of freeBlk/allocBlk and is not a problem with test case.");
                        }
                    }
                    System.out.println("Yours  :-  Output:- "+commOrig.output+" FreeBlk Size:- "+commOrig.freeSize+" AllocBlk Size:- "+commOrig.allocSize);
                    System.out.println("Correct:-  Output:- "+commCorr.output+" FreeBlk Size:- "+commCorr.freeSize+" AllocBlk Size:- "+commCorr.allocSize);
                    System.out.println("Use accompanying jupyter notebook to see further details");
                    System.out.println("-----------------------");
                    return;
                }
            }

        }

        

    }


    static void resetTimeTaken(){
        for(int i=0;i<time_taken.size();i++){
            time_taken.set(i,0.0);
            total_ops.set(i,0);
        }
    }

    static OutputObject testInputs(String file_name,String output_file_name,boolean generateReport) throws FileNotFoundException {
        //noinspection ConstantConditions
        FileReader fileReader = new FileReader(root_path.equals("") ?file_name:root_path+"/"+file_name);
        System.out.println("Starting. Will take some time...");
        while(fileReader.hasNext()){
            if(fileReader.getNext().equals("START"))
                break;
        }
        time_taken = new ArrayList<>();
        total_ops = new ArrayList<>();
        for(int i=0;i<5;i++){
            time_taken.add(0.0);
            total_ops.add(0);
        }

        int totalTestCases = fileReader.getNextInt();

        OutputObject outputObject = new OutputObject(totalTestCases);
        loopThread = new MultithreadingDemo(System.nanoTime());
        loopThread.start();
        for(int numTestCases = 1;numTestCases<=totalTestCases;numTestCases++){

            loopThread.pauseTimer();

            int size;
            size = fileReader.getNextInt();
            DynamicMem obj = null;
            switch (assign_num){
                case 1:
                    obj = new A1DynamicMem(size);
                    break;
                //noinspection ConstantConditions
                case 2:
                    obj = new A2DynamicMem(size,2);
                    break;
                case 3:
                    obj = new A2DynamicMem(size,3);
                    break;
                default:
                    System.out.println("Not a valid assignment number");
                    System.exit(0);
            }
            int totalCommands = fileReader.getNextInt();

            outputObject.addTestCase(totalCommands,size);

            loopThread.setTime(System.nanoTime());
            loopThread.resumeTimer();
            for(int numCommands = 1;numCommands<=totalCommands;numCommands++) {

                String command;
                try {
                    command = fileReader.getNext();
                }catch (Exception e){
                    System.out.println("An exception has occured"+e+"\nnum_commands"+numCommands+" t_case"+numTestCases);
                    return null;
                }
//                if(numTestCases==0 && numCommands==462){
//                    System.out.println("Ok so"+command);
//                }
                int val = -1;

                if(!(command.equals("Defrag") || command.equals("Sanity"))) {
                    val = fileReader.getNextInt();
                }
                handleCases(command,val,obj,outputObject,"Test Case:- "+numTestCases+" Command Number:-"+numCommands);
            }
        }

        loopThread.pauseTimer();
        outputObject.time_taken = time_taken;
        outputObject.total_ops = total_ops;
        save_obj(outputObject,output_file_name,generateReport);
        loopThread.stop();
        return outputObject;
    }

    static void handleCases(String command,int val,DynamicMem obj,OutputObject outputObject,String extra_info){
        int result = -99;
        long time = -1;
        long start;
        loopThread.setLastCommand(command+" "+val+" at "+extra_info);

        switch (command) {
            case "Sanity":
                total_ops.set(1,total_ops.get(1)+1);

                start = System.nanoTime();
                boolean free_san = obj.freeBlk.sanity();
                boolean alloc_san = obj.allocBlk.sanity();
                time = System.nanoTime()-start;
                time_taken.set(1,time_taken.get(1) +  (double) time);
                time_taken.set(0, time_taken.get(0) + (double) time);

                int code = 0;
                if(!free_san){ // 00 if both right // 11 if both false
                    code += 2;
                    System.out.println("Sanity Issue detected in FMB");
                }
                if(!alloc_san){
                    code += 1;
                    System.out.println("Sanity Issue detected in AMB");
                }
                outputObject.addCommand(command,null,code,null,null);
                break;
            case "Defrag":
                total_ops.set(2,total_ops.get(2)+1);

                start = System.nanoTime();
                obj.Defragment();
                time = System.nanoTime()-start;
                time_taken.set(2, time_taken.get(2) + (double) time);
                time_taken.set(0, time_taken.get(0) + (double) time);
                outputObject.addCommand(command,null,null,null,null);
                break;
            case "Alloc":
                total_ops.set(3,total_ops.get(3)+1);
                start = System.nanoTime();
                result = obj.Allocate(val);
                time = System.nanoTime()-start;
                time_taken.set(3, time_taken.get(3) + (double) time);
                time_taken.set(0, time_taken.get(0) + (double) time);
                outputObject.addCommand(command,val,result,null,null);
                break;
            case "Free":
                total_ops.set(4,total_ops.get(4)+1);
                if(val<0){
                    loopThread.setLastCommand("Free "+val+" at "+extra_info);
                    start = System.nanoTime();
                    result = obj.Free(-val);
                    outputObject.addCommand(command,val,result,-1,-val);
                }
                else {
                    int s = getSize(obj.allocBlk);
                    int corr_addr;
                    int corr = -1;
                    if (s != 0) {
                        corr = (val + PRIME) % s;
                        corr_addr = getIthElement(obj.allocBlk, corr);
                    } else {
                        corr_addr = -1;
                    }
                    loopThread.setLastCommand("Free "+corr_addr+" at "+extra_info);
                    start = System.nanoTime();
                    result = obj.Free(corr_addr);
                    outputObject.addCommand(command,val,result,corr,corr_addr);
                }
                time = System.nanoTime()-start;
                time_taken.set(4, time_taken.get(4) + (double) time);
                time_taken.set(0, time_taken.get(0) + (double) time);
                break;
            default:
                total_ops.set(0,total_ops.get(0)-1);
                System.out.println("Not Implemented Yet");
                break;
        }
        total_ops.set(0,total_ops.get(0)+1);
        loopThread.setTime(System.nanoTime());
        loopThread.setLastCommand("Calculation of blk sizes \n Indicates your getNext functionality is " +
                "stucked in loop, either because of cycle in blks aur bugged implementation of getNext(),getFirst() \n"+
                " at "+extra_info);
        outputObject.addToLastCommand(getSize(obj.freeBlk),getSize(obj.allocBlk));

//         outputObject.addToLastCommand(getMBList(obj.freeBlk).s,getMBList(obj.allocBlk));
    }

    static void forceLoop(){
        while(true){
            int i = 0;
        }
    }

    static List<MBElement> getMBList(Dictionary free){
        List<MBElement> mbElements = new ArrayList<>();
        Dictionary start = free.getFirst();
        if(start==null)
            return mbElements;
        while(start!=null){
            mbElements.add(new MBElement(start.address,start.key,start.size));
            start = start.getNext();
        }
        return mbElements;
    }


    static int getIthElement(Dictionary dictionary,int n){
        Dictionary start = dictionary.getFirst();
        for(int i=0;i<n;i++){
            start = start.getNext();
        }
        return start.address;
    }

    static int getSize(Dictionary dict){
        Dictionary start = dict.getFirst();
        if(start==null)
            return 0;
        int i = 0;
        while(start!=null){
            start = start.getNext();
            i++;
        }
        return i;
    }

    static void save_to_text_file(OutputObject obj,String output_file_name){
        StringBuilder outp = new StringBuilder();
        for(TestCase a : obj.testCases){
            for (Command b: a.commands){
                outp.append(b.output).append("\n");
            }
        }
        try {
            FileWriter myWriter = new FileWriter(root_path+"/"+output_file_name+".out");
            myWriter.write(String.valueOf(outp));
            myWriter.close();
            System.out.println("Sys Output File is saved in "+output_file_name+".out");
        } catch (IOException i) {
            i.printStackTrace();
        }

    }

    static void save_obj(OutputObject outputObject,String output_file_name,boolean generate_report){
        save_to_text_file(outputObject,output_file_name);
        if(!generate_report){
            System.out.println("Your actual program execution took " + (time_taken.get(0) / Math.pow(10, 9)) + " seconds");
            System.out.println("for " + total_ops.get(0) + " operations");
            System.out.println("averaging " + (total_ops.get(0) / (time_taken.get(0) / Math.pow(10, 9))) + " operations per second");

            return;
        }
        try {
            System.out.println("Started Saving main output File....");
            System.out.println("This may take some time. Plz be patient....");
            System.out.println("Meanwhile for your info:-");
            System.out.println("Your actual program execution took " + (time_taken.get(0) / Math.pow(10, 9)) + " seconds");
            System.out.println("for " + total_ops.get(0) + " operations");
            System.out.println("averaging " + (total_ops.get(0) / (time_taken.get(0) / Math.pow(10, 9))) + " operations per second");
            System.out.println("Please Wait...");
        }catch (Exception e){
            System.out.println("Error printing stats... No worries");
        }
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(root_path+"/"+output_file_name+".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(outputObject);
            out.close();
            fileOut.close();
            System.out.println("Output data is saved in "+root_path + "/" + output_file_name+".ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    static OutputObject loadObject(String path){
        OutputObject obj;

        try {
            System.out.println("Opening file "+path);
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            obj = (OutputObject) in.readObject();
            in.close();
            fileIn.close();
         }catch(FileNotFoundException e){
             System.out.println("Cannot find .ser file for generating report");
             return null;
         } 
         catch (IOException i) {
            i.printStackTrace();
            return null;
         } catch (ClassNotFoundException c) {
            System.out.println(".ser file not found,Cannot generate first point of difference");
            c.printStackTrace();
            return null;
         }

         return obj;
    }


}



class ArgParser{

    boolean largeTestCase;
    int assign_num;
    boolean generateReport;

    ArgParser(boolean largeTestCase,int assign_num,boolean generateReport){
        this.largeTestCase = largeTestCase;
        this.assign_num = assign_num;
        this.generateReport = generateReport;
    }

    void parseArguments(String[] args){
        for(String a: args){
            switch(a){
                case "--large":
                    this.largeTestCase = true;
                    break;
                case "--report":
                    this.generateReport = true;
                    break;
                default:
                    if(a.matches("-?\\d+")){
                        int n = Integer.parseInt(a);
                        if(n>3 || n<0){
                            System.out.println("Invalid Assignment Number");
                        }else{
                            this.assign_num = n;
                        }
                    }

                }
        }
    }

}


class MBElement implements Serializable{
    int address;
    int key;
    int size;

    MBElement(int address,int key,int size){
        this.address = address;
        this.key = key;
        this.size = size;
    }
}

class Command implements Serializable{
    String type;
    Integer val;
    Integer output;
    List<MBElement> freeBlk;
    List<MBElement> allocBlk;
    Integer allocSize;
    Integer freeSize;
    Integer corr;
    Integer corr_addr;
}

class TestCase implements Serializable{

    int n_commands;
    int size;
    List<Command> commands;

    TestCase(int n_commands,int size){
        this.n_commands = n_commands;
        this.size = size;
        commands = new ArrayList<>();
    }

    void addCommand(String type,Integer val,Integer output,Integer corr,Integer corr_addr){
        Command temp = new Command();
        temp.type = type;
        temp.val = val;
        temp.output = output;
        temp.corr = corr;
        temp.corr_addr = corr_addr;
        commands.add(temp);
    }

}

class OutputObject implements Serializable {

    int num_test;
    List<TestCase> testCases;
    List<Double> time_taken;
    List<Integer> total_ops;

    void addTestCase(int n_commands,int size){
        testCases.add(new TestCase(n_commands,size));
    }

    OutputObject(int num_test){
        this.num_test = num_test;
        testCases = new ArrayList<>();
    }

    void addCommand(String type,Integer val,Integer output,Integer corr,Integer corr_addr){
        TestCase last_case = testCases.get(testCases.size()-1);
        last_case.addCommand(type,val,output,corr,corr_addr);
    }

    void addToLastCommand(List<MBElement> free,List<MBElement> alloc){
        TestCase last_case = testCases.get(testCases.size()-1);
        Command comm = last_case.commands.get(last_case.commands.size()-1);
        comm.freeBlk = free;
        comm.allocBlk = alloc;
    }
    void addToLastCommand(Integer freeSize,Integer allocSize){
        TestCase last_case = testCases.get(testCases.size()-1);
        Command comm = last_case.commands.get(last_case.commands.size()-1);
        comm.freeSize = freeSize;
        comm.allocSize = allocSize;
    }

}


class FileReader{

    Scanner reader;
    String file_name;

    FileReader(String file_name) throws FileNotFoundException {
        this.file_name = file_name;
        File file_obj = new File(file_name);
        reader = new Scanner(file_obj);
    }

    boolean hasNext(){
        return reader.hasNext();
    }

    String getNext(){
        return reader.next();
    }

    int getNextInt(){
        return Integer.parseInt(reader.next());
    }

}

class TestCaseGenerator{

    long seed;
    Random random;
    String name;
    String root_path;
    int assign_num;
    String output_folder;
    long PRIME;


    TestCaseGenerator(long seed,String name,String root_path,int assign_num,String output_folder,int PRIME){
        this.name = name;
        this.root_path = root_path;
        this.assign_num = assign_num;
        this.seed = seed;
        this.random = new Random();
        this.PRIME = PRIME;
        this.output_folder = output_folder;
        random.setSeed(seed);
    }

    String generateTestCases(int test_cases,String suff) throws IOException {
        String file_name = "";
        try {


            File file = new File(root_path+"/"+output_folder);

            //Creating the directory

            //noinspection ResultOfMethodCallIgnored
            file.mkdir();

            file_name = output_folder + "/" + name + "_test_case_" + suff + "_" + seed;
            FileWriter myWriter = new FileWriter(root_path + "/" + file_name+ ".in");


            myWriter.write(this.seed+"\n");
            myWriter.write("START\n");
            myWriter.write(test_cases+"\n");

            for (int i = 0; i < test_cases; i++) {
                if ((100.0 * i / test_cases) % 10 == 0 && i != 0) {
                    System.out.println("Generation " + (100*i/test_cases) + "% complete");
                }
                addCase(1000 * (i + 1), myWriter, test_cases > 1000 && i > 800 && i < 1000);
            }
            System.out.println("Generation of " + suff + " Complete");

            myWriter.close();
        }catch (Exception e){
            System.out.println("Error generating file:- "+e);
            return file_name;
        }
        return file_name;

    }

    void addCase(int max_size,FileWriter f,boolean staticWeightage) throws IOException {
        long total  = 0;
        double free_weightage = random.nextDouble();
        free_weightage = free_weightage*(1-2*0.04) + 0.04;
        int size = (int)(random.nextDouble() * max_size);
        int n_comm = (int)(random.nextDouble()*max_size/100);
        f.write(size+"\n");
        f.write(n_comm+"\n");
        for(int i =0;i<n_comm;i++) {
            if (i % 10 == 0 && !staticWeightage) {
                free_weightage = random.nextDouble();
                free_weightage = free_weightage * (1 - 0.06) + 0.04; // Even distribution from 0.04 to 0.94
            }
            String comm = "";
            Integer val = null;
            double type = random.nextDouble();
            if (type < 0.04) {
                comm = "Sanity";
            } else if (type > (assign_num==1?1:0.94)) {
                comm = "Defrag";
            } else if (type > free_weightage-0.04) { // 0.08 balancing factor
                //alloc:free = 49:41
                comm = "Alloc";
                double rang = random.nextDouble();
                int addn = 0;
                if (rang >= 0.93) {
                    addn = 2;
                }
                if (rang <= 0.07) {
                    val = (int) (random.nextDouble() * 99);
                } else {
                    val = (int) ((Math.pow(10, (addn + 1.6 + random.nextDouble() * 2))) * max_size / Math.pow(10, 6));
                }
            } else {
                comm = "Free";
                double inv = random.nextDouble();
                if (inv > 0.99) {
                    val = -(int) (random.nextDouble() * size);
                } else {
                    val = (int) (random.nextDouble() * Math.pow(10, 7) + PRIME);
                }
            }
            String st = comm + ' ' + (val == null ? "" : val.toString());//('' if val is None else str(val))+'\n'
            f.write(st+"\n");
            total +=n_comm;
        }
    }

}


class CodeEvaluater{

    boolean evaluateCode(String inp,String outp){
        try {
            // inp = outp;
            File my = new File(inp);
            File corr = new File(outp);
            Scanner myReader = new Scanner(my);
            Scanner corrReader = new Scanner(corr);
            int i = 0;
            while (myReader.hasNextLine()) {
                i++;
                String data1 = myReader.nextLine();
                String data2 = corrReader.nextLine();
                if(!data1.equals(data2)){
                    return false;
                }
            }
            if(corrReader.hasNextLine()){
                return false;
            }
            myReader.close();
            corrReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could Not find correct output file");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}


class MultithreadingDemo extends Thread
{
    volatile long time;
    final int max_diff = 5000;
    String lastCommand;
    volatile boolean pause = false;

    boolean checkLoop(){
//        System.out.println("Pause inside fucntion is "+ pause);

        if(this.pause){
            System.out.println("Pause is trie");
            return false;
        }
        double diff = (System.nanoTime()-this.time)/Math.pow(10,6);
        return diff > max_diff;
    }

    MultithreadingDemo(long time){
        super();
        this.time = time;
        this.lastCommand = "";
    }

    public void run()
    {
        while(true) {
            try {
//                System.out.println("Hello");
                if(!this.pause) {
                    if (checkLoop()) {
                        if(checkLoop()) { // Don't know why it works but it does work
                            System.out.println("Your Program execution has stuck in loop");
                            System.out.println("This happened while executing:-");
                            System.out.println(lastCommand);
                            System.out.println("Exiting program execution");
                            System.exit(0);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void setTime(long time){
        this.time = time;
    }

    void setLastCommand(String lastCommand){
        this.lastCommand = lastCommand;
    }

    void pauseTimer(){
        this.pause = true;
    }

    void resumeTimer(){
        this.pause = false;
    }

}