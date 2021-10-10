import Collection.CollectionManager;
import Collection.StudyGroup;
import types.Commands;
import types.FormOfEducation;
import types.Semester;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Server {

    private Socket socket = null;
    private ServerSocket serverSocket = null;
    int port;
    BufferedReader in;
    BufferedWriter out;

    CollectionManager collection = new CollectionManager();

    ObjectOutputStream outobj;
    ObjectInputStream inobj;

    private final List<String> history = new LinkedList<>();

    Scanner scanner = new Scanner(System.in);

    public Server() {
    }

    public void start(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущен");
    }

    public void acceptConnection() throws IOException {
        socket = serverSocket.accept();
        System.out.println("Клиент присоединился");
        this.outobj = new ObjectOutputStream(socket.getOutputStream());
        this.inobj = new ObjectInputStream(socket.getInputStream());
    }

    public void readAndSendBack() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String text = "";

        while (text.equals("over") == false) {
            text = in.readLine();
            System.out.println("client: " + text);
            out.write("Пидорас написал \""+text+"\" \n");
            out.flush();
        }
    }




    public void readCommand(Statement stat) throws IOException, ClassNotFoundException, SQLException {

        boolean check=true;
        while (check) {


            if(System.in.available()>0){
                String line1;
                Scanner scanner = new Scanner(System.in);
                if ((line1 = scanner.nextLine()) != null) {
                    line1 = line1.trim();
                    if (line1.equals("save")) {
                        collection.save(stat);
                        System.out.println("saved");
                    } else if (line1.equals("exit")) {
                        System.exit(0);
                    }
                }
            }


            try {
                CommandInfoZ pog = (CommandInfoZ) inobj.readObject();
                System.out.println(pog);

                if (pog.commNumber==4){
                    StudyGroup p = new StudyGroup(collection.GetMaxKey(stat)+1,pog.name,pog.coordinates.toString(),pog.creationDate, pog.studentsCount,pog.formOfEducation,pog.semesterEnum);
                    collection.insert(p);
                    outobj.writeObject("Студент Добавлен");


                    String sqlAdd;

                    //int idDB=(int) collection.GetMaxKey()+1;
                    //sqlAdd = "INSERT INTO Students (id, name, coordinates, creationDate, studentsCount, formOfEducation, semesterEnum) VALUES " +
                    //        "("+ idDB+", '"+pog.name+"', '"+pog.coordinates.toString()+"','"+ pog.creationDate.toString()+"','"+(int) pog.studentsCount+"', '"+pog.formOfEducation.toString()+"', '"+pog.semesterEnum.toString()+"')";
                    //sqlAdd = "INSERT INTO Students (id, name, coordinates, creationDate, studentsCount, formOfEducation, semesterEnum) VALUES " +
                    //        "(nextval('id'), '"+pog.name+"', '"+pog.coordinates.toString()+"','"+ pog.creationDate+"','"+(int) pog.studentsCount+"', '"+pog.formOfEducation.toString()+"', '"+pog.semesterEnum.toString()+"')";

                   // stat.executeUpdate(sqlAdd);

                }else if(pog.commNumber==5){
                    if (collection.get(pog.id) == null) {
                        outobj.writeObject("Элемент с заданным ключём отстутствует");
                    } else {
                        collection.remove(pog.id);
                        StudyGroup p = new StudyGroup(pog.id,pog.name,pog.coordinates.toString(),pog.creationDate, pog.studentsCount,pog.formOfEducation,pog.semesterEnum);
                        collection.insert(p);
                        outobj.writeObject("Объект был обновлён");
                    }

                }else if(pog.commNumber==3){
                    ResultSet resultSet = stat.executeQuery("SELECT * FROM students");

                    while (resultSet.next()) {
                        System.out.print(resultSet.getInt("id")+" "+resultSet.getString("name"));
                        System.out.println();
                    }
                    outobj.writeObject(collection.getPersonsA().toString());

                }else if(pog.commNumber==6){
                    if (collection.get(pog.id) == null) {
                        outobj.writeObject("Элемент с заданным ключём отстутствует");
                    } else {
                        collection.remove(pog.id);
                        outobj.writeObject("Объект был удалён");
                    }

                }else if(pog.commNumber==1){
                    outobj.writeObject(collection.description);

                }else if(pog.commNumber==2){
                    outobj.writeObject( "Тип коллекции: " + collection.getClass()+"\n" +
                                        "Дата инициализации: " + collection.getInitializationDate()+"\n" +
                                        "Количество элементов: " + collection.getSize()+"\n");

                }else if(pog.commNumber==7){
                    collection.clear();
                    outobj.writeObject("Коллекция была очищена");

                }else if(pog.commNumber==12){
                    outobj.writeObject(history.toString());

                }else if(pog.commNumber==11){
                    if (collection.get(pog.id) == null) {
                        outobj.writeObject("Элемент с заданным ключём отстутствует.");
                    } else {
                        collection.removeLower(pog.id);
                        outobj.writeObject("Элементы убраны");
                    }
                }else if(pog.commNumber==13){
                    String list = collection.FilterSemester(pog.semesterEnum);
                    if (list==""){
                        outobj.writeObject("Студентов с таким семестром не найдено");
                    }else {
                        outobj.writeObject(list);
                    }
                }else if(pog.commNumber==14){
                    outobj.writeObject(collection.subStringSearcher(pog.name));
                }else if(pog.commNumber==17){
                    boolean login_exists;
                    ResultSet resultSet = stat.executeQuery("SELECT EXISTS (SELECT 1 FROM users WHERE NAME='"+pog.login+"')");
                    resultSet.next();
                    login_exists=resultSet.getBoolean("exists");
                    System.out.println(login_exists);
                    if (pog.text.equals("yes")){
                        if(login_exists==true){
                            resultSet = stat.executeQuery("SELECT * FROM users WHERE NAME='"+pog.login+"'");
                            resultSet.next();
                            if(resultSet.getString("password").equals(pog.password)){
                                outobj.writeObject("Вход выполнен");
                            }else{
                                outobj.writeObject("Неверный пароль");
                            }
                        }else{
                            outobj.writeObject("Юзер с таким логином не найден");
                        }
                    }else if(pog.text.equals("no")){
                        //login_exists=stat.executeUpdate("" );
                        if (login_exists==true){
                            outobj.writeObject("Такой Юзер уже есть");
                        }else{
                            stat.executeUpdate("INSERT INTO users(name,password) VALUES('"+pog.login+"','"+pog.password+"')");
                            outobj.writeObject("Зарегистрировано");
                        }
                    }
                }

                if(pog.commNumber!=17) {
                    history.add(Commands.getCommand(pog.commNumber).toString());
                }
                if (history.size()>14){
                    history.remove(0);
                }

            }catch (SocketException e){
                System.out.println("Потеряно соединение с клиентом");
                check=false;
            }catch (SQLException e){
                e.printStackTrace();
            }catch(EOFException e){
                System.out.println("отеряно соединение с клиентом");
                check=false;
            }
        }
    }

    public void saveCollection(Statement stat) throws SQLException {
        collection.save(stat);
    }

    public void loadCollectionFromFile(String filename) throws JAXBException {
        collection.loadFromFile(filename);
    }

    public void loadCollectionFromDataBase(Statement stat) throws SQLException {
        ResultSet resultSet = stat.executeQuery("SELECT * FROM students");
        while (resultSet.next()) {
            //System.out.print(resultSet.getInt("id")+" "+resultSet.getString("name"));
            //System.out.println(resultSet.getString("name"));
            StudyGroup p = new StudyGroup(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getString("coordinates"),resultSet.getString("creationdate"), resultSet.getInt("studentscount"), FormOfEducation.valueOf(resultSet.getString("formofeducation")), Semester.valueOf(resultSet.getString("semesterenum")));
            collection.insert(p);
            System.out.println();
        }
    }

}
