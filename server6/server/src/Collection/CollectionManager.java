package Collection;

import types.Semester;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CollectionManager implements Serializable {
    @XmlElement(name = "groupA")
    public static TreeMap<Long,StudyGroup> groupsA = new TreeMap<>();
    public long biggestId;
    private final LocalDate initializationDate;

    public CollectionManager() {
        initializationDate = LocalDate.now();
    }


    public void insert(StudyGroup element) {
        groupsA.put(element.getId(),element);
    }

    public void remove(long id){
        groupsA.remove(id);
    }

    public StudyGroup get(long id) {
        return groupsA.get(id);
    }

    public void save(Statement stat) throws SQLException {/*
        StringWriter sw = new StringWriter();
        try {
            CollectionManager people = new CollectionManager();
            JAXBContext context = JAXBContext.newInstance(CollectionManager.class);
            Marshaller jaxbMarshaller = context.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            jaxbMarshaller.marshal(people, new File("Data.xml"));

            jaxbMarshaller.marshal(people, sw);
        } catch (JAXBException e) {
            e.printStackTrace();
        }*/
        stat.executeUpdate("DELETE FROM students");
        for(Map.Entry<Long,StudyGroup> pog : groupsA.entrySet()){
            stat.executeUpdate("INSERT INTO Students (id, name, coordinates, creationDate, studentsCount, formOfEducation, semesterEnum) VALUES " +
                    "(nextval('id_creator'), '"+pog.getValue().getName()+"', '"+pog.getValue().getCoordinates()+"','"+ pog.getValue().getCreationDate()+"','"+pog.getValue().getStudentsCount()+"', '"+pog.getValue().getForm()+"', '"+pog.getValue().getSemester()+"')");
        }
    }

    public TreeMap<Long,StudyGroup> getPersonsA(){
        return groupsA;
    }

    public long GetMaxKey(Statement stat) throws NoSuchElementException, SQLException {
        try {
            biggestId = groupsA.lastKey();
        } catch (NoSuchElementException e){
            ResultSet resultSet=stat.executeQuery("SELECT last_value FROM id_creator");
            while (resultSet.next()) {
                biggestId=resultSet.getInt("last_value");
            }
        }
        return biggestId;
    }

    /*public void CorrectDataReader(){
        for(Map.Entry<Long,StudyGroup> entry : groupsA.entrySet()){
            if(!(entry.getValue().getStudentsCount() > 0) || (entry.getValue().getName() == null) || (entry.getValue().getName().equals(""))
                    || entry.getValue().getForm() == null || entry.getValue().getCoordinates().getX() > 585){
                System.out.println("Invalid values in file");
                System.exit(0);
            }
        }
    }*/

    CollectionManager people;
    public void loadFromFile(String filename) throws JAXBException {
        try {
            File file = new File(filename);
            JAXBContext context = JAXBContext.newInstance(CollectionManager.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            people = (CollectionManager) unmarshaller.unmarshal(file);
        } catch (UnmarshalException e){
            System.out.println("Invalid file");
            System.exit(0);
        }
    }

    public void removeLower(long id) throws NoSuchElementException{
        while (groupsA.firstKey() < id) {
            groupsA.remove(groupsA.firstKey());
        }
    };

    public String FilterSemester(Semester s){
        String list="";
        for(Map.Entry<Long,StudyGroup> entry : groupsA.entrySet()) {
            if (entry.getValue().getSemester().equals(s)){
                list=list+entry.toString();
            }
        }
        return list;
    }

    public String subStringSearcher(String subString){
        StringBuilder box = new StringBuilder();
        for(Map.Entry<Long,StudyGroup> entry : groupsA.entrySet()) {
            if (entry.getValue().getName().startsWith(subString)){
                box.append(entry.getValue().toString()).append("\n");
            }
        }
        return box.toString();
    }



    public LocalDate getInitializationDate() {
        return initializationDate;
    }

    public int getSize() {
        return groupsA.size();
    }

    public void clear() {
        groupsA.clear();
    }
    public String description=
"1 help : ?????????????? ?????????????? ???? ?????????????????? ???????????????? \n" +
"2 info : ?????????????? ?? ?????????????????????? ?????????? ???????????? ???????????????????? ?? ?????????????????? (??????, ???????? ??????????????????????????, ???????????????????? ?????????????????? ?? ??.??.) \n" +
"3 show : ?????????????? ?? ?????????????????????? ?????????? ???????????? ?????? ???????????????? ?????????????????? ?? ?????????????????? ?????????????????????????? \n" +
"4 insert : ???????????????? ?????????? ?????????????? ?? ???????????????? ???????????? \n" +
"5 update : ???????????????? ???????????????? ???????????????? ??????????????????, id ???????????????? ?????????? ?????????????????? \n" +
"6 remove : ?????????????? ?????????????? ???? ?????????????????? ???? ?????? ?????????? \n" +
"7 clear : ???????????????? ?????????????????? \n" +
"9 execute_script : ?????????????? ?? ?????????????????? ???????????? ???? ???????????????????? ??????????. ?? ?????????????? ???????????????????? ?????????????? ?? ?????????? ???? ????????, ?? ?????????????? ???? ???????????? ???????????????????????? ?? ?????????????????????????? ????????????. \n" +
"10 exit : ?????????????????? ?????????????????? (?????? ???????????????????? ?? ????????) \n" +
"11 remove_lower : ?????????????? ???? ?????????????????? ?????? ????????????????, ??????????????, ?????? ???????????????? \n" +
"12 history : ?????????????? ?????????????????? 14 ???????????? (?????? ???? ????????????????????) \n" +
"13 filter_by_semester_enum : ?????????????? ????????????????, ???????????????? ???????? semesterEnum ?????????????? ?????????? ?????????????????? \n" +
"14 filter_starts_with_name : ?????????????? ????????????????, ???????????????? ???????? name ?????????????? ???????????????????? ?? ???????????????? ?????????????????? \n";



}
