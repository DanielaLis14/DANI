package pe.edu.upeu.farmafx;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pe.edu.upeu.farmafx.enums.ViewRoute;
import pe.edu.upeu.farmafx.utils.StageManager;
import pe.edu.upeu.farmafx.utils.ViewNavigator;

@SpringBootApplication
public class FarmaFxApplication extends Application {

    private ConfigurableApplicationContext ctx;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(FarmaFxApplication.class);
        builder.application().setWebApplicationType(WebApplicationType.NONE);
        ctx = builder.run(getParameters().getRaw().toArray(new String[0]));

        // Inyectar contexto Spring en ViewNavigator UNA SOLA VEZ
        ViewNavigator viewNavigator = ctx.getBean(ViewNavigator.class);
        viewNavigator.setApplicationContext(ctx);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Obtener StageManager del contexto Spring (no se puede usar @Autowired en Application)
        StageManager stageManager = ctx.getBean(StageManager.class);
        stageManager.setPrimaryStage(stage);

        // Cargar vista login
        ViewNavigator viewNavigator = ctx.getBean(ViewNavigator.class);
        Parent loginView = viewNavigator.loadView(ViewRoute.TEST_LOGIN.getPath());

        // Configurar y mostrar stage
        Scene scene = new Scene(loginView);
        stage.setScene(scene);
        stage.setTitle("FarmaFx");
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        stage.show();
    }
}
