import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class MainServer {
    public static void main(String[] args) throws IOException, ClassNotFoundException, JAXBException, SQLException {
        Server server = new Server();
        server.start(5000);
        //server.loadCollectionFromFile("Data.xml");


        Connection connection;
        Class.forName("org.postgresql.Driver");
        /*connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                "postgres","admin"); //комп*/
        connection = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs",
                "s312503","xfn949"); //гелиос с гелиоса
        /*connection = DriverManager.getConnection("jdbc:postgresql://localhost:1705/studs",
                "s312503","xfn949"); //гелиос с компа*/

        Statement stat;
        stat=connection.createStatement();

        server.loadCollectionFromDataBase(stat);

        while(true){
            System.out.println("Поиск подключения");
            server.acceptConnection();
            server.readCommand(stat);
            server.saveCollection(stat);
        }
        //server.readObject();
    }
}
