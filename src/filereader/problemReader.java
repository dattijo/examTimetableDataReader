/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filereader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PhDLab
 */

class Exam
{
    int examId,examDuration,studentsCount=0;
    ArrayList<Integer> enrollmentList = new ArrayList<>();
    
    Exam(int id, int duration)
    {
        examId=id;
        examDuration=duration;
    }
    
    void addStudent(Integer student)
    {
        enrollmentList.add(student);
        studentsCount++;
    }
}

public class problemReader
{      
    int numberOfExams,numberOfPeriods,numberOfRooms;
    
    Map <Integer,List> studentMap = new HashMap<>();
    ArrayList<Exam> examVector = new ArrayList<Exam>();
    
    problemReader(String file) throws IOException 
    {
        
        InputStream in = getClass().getResourceAsStream(file);
        if (in == null) 
        {
            in = new FileInputStream(file);
        }
    
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);

        StreamTokenizer token = new StreamTokenizer(br);
        
        token.eolIsSignificant(true);
        boolean found ;
        found = false ;

        readExams(token,found);
        readPeriods(token,found);
        readRooms(token,found);
        readConstraints(token,found);
        readWeightings(token,found);
        
        System.out.println("Reading Successful.");
    } 
    
    void readExams(StreamTokenizer tok, boolean fnd) throws IOException
    {
        tok.nextToken();
        while(!fnd) 
        {
            if ((tok.sval != null) && ((tok.sval.compareTo("Exams") == 0)))
                fnd = true ;
            else
                tok.nextToken() ;
        }

        tok.nextToken() ;
        tok.nextToken() ;

        numberOfExams =  (int)tok.nval ;
        System.out.println("Number of Exams: "+numberOfExams);
        tok.nextToken();tok.nextToken();tok.nextToken();

        System.out.println("Reading Token: "+tok.nval);
        addExam(tok);
             

        //Read Enrollments
        fnd=false;
        int t=0;
        while(!fnd) 
        {
            if ((tok.sval != null) && ((tok.sval.compareTo("Periods") == 0)))
            {
                tok.nextToken();
                tok.nextToken();
                numberOfPeriods=(int)tok.nval;
                System.out.println("Finished Reading Enrollments.");
                System.out.println("No. of Periods to be read = "+numberOfPeriods);
                fnd = true ;
            }                
            else
            {                
                t = tok.nextToken();

                switch(t)
                {                    
                    case StreamTokenizer.TT_EOL:    
                        //line = tok.lineno()-1;
                        //System.out.println("Finished Reading Exam "+ (line-1) + "\n" + examVector.get(line-2).studentsCount + " student(s) added : " + examVector.get(line-2).enrollmentList);
                        tok.nextToken(); 
                        System.out.println("Now reading: "+(int)tok.nval);
                        addExam(tok);                        
                        break;
                    case StreamTokenizer.TT_NUMBER: 
                        
                        Integer currentStudent = (int)tok.nval;                 
                        examVector.get(tok.lineno()-2).addStudent(currentStudent);
                        System.out.println("Student "+(int)tok.nval+" added successfully.");
                        if(!studentMap.containsKey(currentStudent))
                        {                            
                            List <Integer> examList = new ArrayList();
                            studentMap.put(currentStudent, examList);                            
                        }
                        studentMap.get(currentStudent).add(tok.lineno()-1);
                        break;
                }                
            }
        }

        //Print Student Map
        int studentCount=0;
        for(Map.Entry<Integer,List> entry : studentMap.entrySet())            
        {
            System.out.println("Student "+ (++studentCount) + "{ " + entry.getKey() + "}: Exams = " + entry.getValue());
        }

        //Initialize Conflict Matrix
        ArrayList <ArrayList<Integer>> conflictMatrix = new ArrayList<>(numberOfExams);
        for(int i=0;i<=numberOfExams-1;i++)
        {            
            conflictMatrix.add(new ArrayList(numberOfExams));
            for(int j=0;j<=numberOfExams-1;j++)
            {
                conflictMatrix.get(i).add(0);
            }
        }
        //Generate Conflict Matrix
        ArrayList cleared = new ArrayList();
        System.out.println("Total Exams: "+examVector.size());
        examVector.forEach((e)->System.out.println(e.examId));
        
        for(int currExam=0; currExam<=examVector.size()-2;currExam++)
        {                        
            System.out.println("Current Exam: " + examVector.get(currExam).examId+ " with "+ examVector.get(currExam).enrollmentList.size()+" students");
            int student;
            cleared.clear();
            for(int currStudent=0; currStudent<=examVector.get(currExam).enrollmentList.size()-1;currStudent++)
            {
                student = examVector.get(currExam).enrollmentList.get(currStudent);
                if(cleared.contains(student))continue;
                cleared.add(student);
                System.out.println("Current Student: " + student);
                //int conflictCount=0;
                for(int nextExam=currExam+1;nextExam<=examVector.size()-1;nextExam++)
                {                   
                    System.out.println("Next Exam: " + examVector.get(nextExam).examId);
                    if(examVector.get(nextExam).enrollmentList.contains(student))
                    {
                        //conflictCount++;
                        System.out.println("Student "+student +" found in both exams "+ currExam +" and "+ nextExam);
                        System.out.println();
                        int tmpEnrollment =  conflictMatrix.get(currExam).get(nextExam);
                        System.out.println("Previous conflict :"+tmpEnrollment);
                        tmpEnrollment++;  
                        conflictMatrix.get(currExam).set(nextExam, tmpEnrollment);
                        conflictMatrix.get(nextExam).set(currExam, tmpEnrollment);
                        //conflictMatrix.get(currExam).remove(nextExam);
                        //conflictMatrix.get(currExam).add(nextExam, tmpEnrollment);
                        //conflictMatrix.get(currExam).remove(currExam);
                        //conflictMatrix.get(nextExam).add(currExam, tmpEnrollment);
                    }
                }
            }
        }

        //Display ConflictMatrix
        System.out.println("DISPLAYING CONFLICT MARIX:\n");
        for(int i=0;i<numberOfExams;i++)
        {
            for(int j=0;j<numberOfExams;j++)
            {
                System.out.print(conflictMatrix.get(i).get(j)+", ");
            }
            System.out.println();
        }  
    }
    
    void addExam(StreamTokenizer tok)            
    {
        
        int line = tok.lineno()-1;
        if(line<=numberOfExams)
        {
            System.out.println("Token is @ line: "+tok.lineno());
            examVector.add(new Exam(line-1,(int)tok.nval));        
            System.out.println("Exam "+ (line-1) + " added. Duration = " + examVector.get(line-1).examDuration);  
            examVector.forEach((e)->System.out.println(e.examId));
        }                
    }
    
    void readPeriods(StreamTokenizer tok, boolean fnd) throws IOException
    {
    //Read Periods
        fnd=false;
        int t;
        while(!fnd) 
        {
            if ((tok.sval != null) && ((tok.sval.compareTo("Rooms") == 0)))
            {
                tok.nextToken();
                tok.nextToken();
                numberOfRooms=(int)tok.nval;
                System.out.println("Finished Reading Periods.");
                System.out.println("Number of Periods = "+numberOfRooms);
                fnd = true ;
            }                
            else
            {                                                   
                t = tok.nextToken();
                switch(t)
                {
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_NUMBER:                    
                        System.out.println("nextToken():"+tok.nval);
                        break;
                }
            }
        }
    }
    
    void readRooms(StreamTokenizer tok, boolean fnd) throws IOException
    {
    //Read Rooms
        fnd=false;int t;
        while(!fnd) 
        {
            if ((tok.sval != null) && ((tok.sval.compareTo("PeriodHardConstraints") == 0)))
            {
                tok.nextToken();
                System.out.println("Finished Reading Rooms.");
                fnd = true ;
            }                
            else
            {                                                   
                t = tok.nextToken();
                switch(t)
                {
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_NUMBER:                    
                        System.out.println("nextToken():"+tok.nval);
                        break;
                }
            }
        }
    }
    
    void readConstraints(StreamTokenizer tok, boolean fnd) throws IOException
    {
        //Read PeriodHardConstraints
        fnd=false;int t;
        while(!fnd) 
        {
            if ((tok.sval != null) && ((tok.sval.compareTo("RoomHardConstraints") == 0)))
            {
                tok.nextToken();
                tok.nextToken();
                numberOfRooms=(int)tok.nval;
                System.out.println("Finished Reading PeriodHardConstraints.");
                fnd = true ;
            }                
            else
            {                                                   
                t = tok.nextToken();
                switch(t)
                {
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_NUMBER:                    
                        System.out.println("nextToken():"+tok.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        System.out.println("nextToken():"+tok.sval);
                        break;
                }
            }
        }

        //Read RoomHardConstraints
        fnd=false;
        while(!fnd) 
        {
            if ((tok.sval != null) && ((tok.sval.compareTo("InstitutionalWeightings") == 0)))
            {
                tok.nextToken();
                tok.nextToken();
                numberOfRooms=(int)tok.nval;
                System.out.println("Finished Reading RoomHardConstraints.");
                fnd = true ;
            }                
            else
            {                                                   
                t = tok.nextToken();
                switch(t)
                {
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_NUMBER:                    
                        System.out.println("nextToken():"+tok.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        System.out.println("nextToken():"+tok.sval);
                        break;
                }
            }
        }
    }
    
    void readWeightings(StreamTokenizer tok, boolean fnd) throws IOException
    {
    //Read InstitutionalWeightings
        int t = tok.nextToken();    //WATCHOUT
        while(t != StreamTokenizer.TT_EOF)
            {                               
                switch(t)
                {
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_NUMBER:                    
                        System.out.println("nextToken():"+tok.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        System.out.println("nextToken():"+tok.sval);
                        break;
                }
                t= tok.nextToken();
            }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            problemReader objproblemReader = new problemReader("C:/Users/PhDLab/Documents/NetBeansProjects/examTimetableDataReader/exam_comp_set00.exam");
        }
        catch(Exception e)
        {
            new IOException(e);
        }
        
    }    
}
