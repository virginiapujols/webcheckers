package com.webcheckers.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.webcheckers.appl.GameCenter;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import static spark.Spark.halt;

/**
 * The Web Controller for the Home page.
 *
 * @author <a href='mailto:add5980@rit.edu'>Andrew DiStasi</a>
 */
public class GetHomeRoute implements TemplateViewRoute {

    // Constants
    static final String VIEW_NAME = "home.ftl";

    // Attributes
    private final GameCenter gameCenter;

    //
    // Constructor
    //

    /**
    * The constructor for the {@code GET /} route handler.
    *
    * @param gameCenter
    *    The {@link GameCenter} for the application.
    */
    GetHomeRoute(final GameCenter gameCenter) {
        Objects.requireNonNull(gameCenter, "gameCenter must not be null");

        this.gameCenter = gameCenter;
    }

  @Override
  public ModelAndView handle(Request request, Response response) {
    Map<String, Object> vm = new HashMap<>();
    vm.put("title", "Welcome!");

    if(gameCenter.isInGame(request.session().attribute("username"))) {
        response.redirect("/game");
        halt();
        return null;
    }

    vm.put("usernames", gameCenter.getAvailablePlayers());

    if(request.session().attribute("username") != null) {
        vm.put("username", request.session().attribute("username"));
    }

    return new ModelAndView(vm , "home.ftl");
  }
}