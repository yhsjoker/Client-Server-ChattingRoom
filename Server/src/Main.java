import Server.CLI;
import Server.Log;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if(Log.start(args[0])) {
            new CLI();
        }
    }
}