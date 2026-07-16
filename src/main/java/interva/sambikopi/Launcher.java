package interva.sambikopi;

public class Launcher {
    public static void main(String[] args) {
        try {
            Class<?> appClass = Class.forName("interva.sambikopi.App");
            appClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
