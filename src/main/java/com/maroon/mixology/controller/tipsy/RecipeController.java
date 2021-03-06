package com.maroon.mixology.controller.tipsy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import com.maroon.mixology.entity.User;
import com.maroon.mixology.entity.type.ActionType;
import com.maroon.mixology.entity.Bar;
import com.maroon.mixology.entity.EquipmentType;
import com.maroon.mixology.entity.Game;
import com.maroon.mixology.entity.Recipe;
import com.maroon.mixology.entity.Role;
import com.maroon.mixology.entity.Step;
import com.maroon.mixology.exchange.request.EquipmentProductRequest;
import com.maroon.mixology.exchange.request.EquipmentRequest;
import com.maroon.mixology.exchange.request.RecipeRequest;
import com.maroon.mixology.exchange.request.StepRequest;
import com.maroon.mixology.exchange.response.ApiResponse;
import com.maroon.mixology.exchange.response.EquipmentProductResponse;
import com.maroon.mixology.exchange.response.EquipmentResponse;
import com.maroon.mixology.exchange.response.EquipmentTypeResponse;
import com.maroon.mixology.exchange.response.RecipeResponse;
import com.maroon.mixology.exchange.response.StatResponse;
import com.maroon.mixology.exchange.response.StepResponse;
import com.maroon.mixology.exchange.response.UnitResponse;
import com.maroon.mixology.exchange.response.brief.BriefUserResponse;
import com.maroon.mixology.repository.BarRepository;
import com.maroon.mixology.repository.GameRepository;
import com.maroon.mixology.repository.RecipeRepository;
import com.maroon.mixology.repository.StepRepository;
import com.maroon.mixology.repository.UserRepository;
import com.maroon.mixology.security.CurrentUser;
import com.maroon.mixology.service.BarService;
import com.maroon.mixology.service.EquipmentTypeService;
import com.maroon.mixology.service.GameService;
import com.maroon.mixology.service.RecipeService;
import com.maroon.mixology.service.UnitService;
import com.maroon.mixology.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/tipsy/recipe")
public class RecipeController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private StepRepository stepRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private BarRepository barRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BarService barService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private EquipmentTypeService equipmentTypeService;

    @Autowired
    private UnitService unitService;
    
    @Autowired
    private GameService gameService;

    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    @PostMapping("/createRecipe")
    public ResponseEntity<?> createNewRecipe(@CurrentUser UserDetails currentUser, @Valid @RequestBody RecipeRequest recipeRequest) {
        try{
            //we get the current user by getting their email address
            User user = userRepository.findByEmail(currentUser.getUsername());
            //We need to build the recipe  
            Recipe recipe = new Recipe();
            recipe.setName(recipeRequest.getName());
            recipe.setDescription(recipeRequest.getDescription());
            recipe.setImage(recipeRequest.getImg());
            recipe.setAuthor(user);
            //Build the Steps from Step requests    
            ArrayList<Step> steps = new ArrayList<Step>();
            for (StepRequest s : recipeRequest.getSteps()){
                steps.add(new Step(
                    s.getEquipmentToDo(),
                    s.getEquipmentDoing(),
                    s.getEquipmentProduct(),
                    ActionType.valueOf(s.getAction()),
                    s.getValue(),
                    unitService.findByName(s.getUnit())
                ));
            }
            recipe.setSteps(steps);
            //Build the equipments from the Equipment Available
            Set<EquipmentResponse> equipmentsAvailable = new HashSet<EquipmentResponse>();
            for (EquipmentRequest e : recipeRequest.getEquipmentsAvailable()){
                EquipmentType eT = equipmentTypeService.findByName(e.getEquipmentType());
                equipmentsAvailable.add(new EquipmentResponse(
                    e.getName(),
                    e.getImg(),
                    new EquipmentTypeResponse(
                            eT.getName(),
                            eT.getActionsToDo(),
                            eT.getActionsDoing()
                    ))
                );
            }
            Set<EquipmentProductResponse> equipmentProducts = new HashSet<EquipmentProductResponse>();
            for (EquipmentProductRequest e : recipeRequest.getEquipmentProducts()){
                EquipmentType eT = equipmentTypeService.findByName(e.getEquipmentType());
                equipmentProducts.add(new EquipmentProductResponse(
                    e.getName(),
                    e.getImg(),
                    new EquipmentTypeResponse(
                            eT.getName(),
                            eT.getActionsToDo(),
                            eT.getActionsDoing()
                    ),
                    e.getTags())
                );
            }
            recipe.setPublished(recipeRequest.getPublished()); //set published
            if(recipe.isPublished() && recipe.getSteps().size() == 0){ //really bad secure design but it works :(
                //make sure the recipe does not have 0 steps
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "You are unable to publish a recipe with 0 steps"),
                HttpStatus.BAD_REQUEST);
            }
            recipe.setEquipmentsAvailable(equipmentsAvailable);
            recipe.setEquipmentProducts(equipmentProducts);
            stepRepository.saveAll(steps); //Will this work?
            recipeRepository.save(recipe);
            return ResponseEntity.ok(new ApiResponse(true, "Recipe creation was succesfully submitted and saved in the database!"));
        } catch (Exception e) {
            logger.error("Recipe was unable to be created.", e);
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe was unable to be created. Error: " + e.toString()),
                        HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{recipeID}")
    public ResponseEntity<?> getRecipe(@CurrentUser UserDetails currentUser, @PathVariable(value = "recipeID") String recipeID) {
        try{
            User user = userService.findByEmail(currentUser.getUsername());
            //we have to query the recipe from Mongo
            Recipe recipe = recipeService.findById(recipeID);
            if(recipe == null){
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe with the ID \"" + recipeID +"\" was not found."),
                HttpStatus.NOT_FOUND);
            }
            // We have the recipe, now lets build a recipe Response
            // Unless you are the author, you cant view an unpublished recipe
            if(recipe.isPublished() || recipe.getAuthor().getId().equals(user.getId())){
                BriefUserResponse author = new BriefUserResponse(
                    recipe.getAuthor().getNickname(), 
                    recipe.getAuthor().getFirstName() + " " + recipe.getAuthor().getLastName(), 
                    recipe.getAuthor().getProfilePic()
                    );

                //Need to build Steps
                ArrayList<StepResponse> steps = new ArrayList<StepResponse>();
                for (Step s : recipe.getSteps()){
                    steps.add(new StepResponse(
                        s.getEquipmentToDo(),
                        s.getEquipmentDoing(),
                        s.getEquipmentProduct(),
                        s.getAction(),
                        s.getValue(),
                        new UnitResponse(
                            s.getUnit().getName(), 
                            s.getUnit().getType(),
                            s.getUnit().getUsMeasurement(), 
                            s.getUnit().getMetricMeasurement()) 
                        ));
                }
                //lets build our response
                RecipeResponse recipeResponse = new RecipeResponse(
                    recipe.getName(),
                    recipe.getDescription(),
                    recipe.getImage(),
                    author,
                    recipe.isPublished(),
                    steps, //StepResponses 
                    recipe.getEquipmentsAvailable(), //EquipmentResponses
                    recipe.getEquipmentProducts()
                );
                return ResponseEntity.ok(recipeResponse);
            }
            else{
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "A published recipe with that ID was not found."),
                        HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Recipe was unable to be loaded.", e);
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe was unable to be loaded. Error: " + e.toString()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{recipeID}/stats")
    public ResponseEntity<?> getRecipeStats(@CurrentUser UserDetails currentUser, @PathVariable(value = "recipeID") String recipeID) {
        try{
            User requester = userService.findByEmail(currentUser.getUsername());
            //we have to query the recipe from Mongo
            Recipe recipe = recipeService.findById(recipeID);
            if(recipe == null){
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe with the ID \"" + recipeID +"\" was not found."),
                HttpStatus.NOT_FOUND);
            }
            // We have the recipe, now lets build an array of Game Responses
            // Unless you are the author, you cant view an unpublished recipe
            if(recipe.isPublished() && recipe.getAuthor().getId().equals(requester.getId())){//only works if requster is author and is Published
                //query all games based on the recipe both complete and incomplete
                List<Game> games = gameService.findByRecipe(recipe);
                //All of the relevant game sessions, lets make an array of StatResponses
                Set<StatResponse> statResponses = new HashSet<StatResponse>();
                for(Game g: games){
                    statResponses.add(new StatResponse(
                        new BriefUserResponse(
                            g.getPlayer().getNickname(), 
                            g.getPlayer().getFirstName() + " " + recipe.getAuthor().getLastName(), 
                            g.getPlayer().getProfilePic()),
                        g.getProgress(),
                        g.isCompleted()
                    ));
                }
                return ResponseEntity.ok(statResponses);
            }
            else{
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Statistics for this recipe were not found."),
                        HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Statistics were unable to be loaded.", e);
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Statistics were unable to be loaded. Error: " + e.toString()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{recipeID}/changeSettings")
    public ResponseEntity<?> changeRecipeSettings(@PathVariable(value = "recipeID") String recipeID, @CurrentUser UserDetails currentUser, @Valid @RequestBody RecipeRequest recipeRequest) {
        try{
            // we get the current user by getting their email address
            User requester = userService.findByEmail(currentUser.getUsername());
            boolean isAdmin = false;
            //Check if the user is an admin
            for (Role r : requester.getRoles()){
                if(r.getName().equals("ADMIN")){
                    isAdmin = true;
                }
            }
            //Find our recipe
            Recipe recipe = recipeService.findById(recipeID);
            if(recipe == null){
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe with the ID \"" + recipeID +"\" was not found."),
                HttpStatus.NOT_FOUND);
            }
            //check if the requester is the author or an Admin
            if(recipe.getAuthor().getId().equals(requester.getId()) || isAdmin){
                recipe.setName(recipeRequest.getName());
                recipe.setDescription(recipeRequest.getDescription());
                recipe.setImage(recipeRequest.getImg());
                //Build the Steps from Step requests
                // We should redo all the steps from the step request
                if(recipeRequest.getNewSteps()){ //make sure this boolean is a thing
                    // we have to delete the steps saved
                    stepRepository.deleteAll(recipe.getSteps());
                    // now, lets add new steps
                    ArrayList<Step> newSteps = new ArrayList<Step>();
                    for (StepRequest s : recipeRequest.getSteps()){
                        newSteps.add(new Step(
                            s.getEquipmentToDo(),
                            s.getEquipmentDoing(),
                            s.getEquipmentProduct(),
                            ActionType.valueOf(s.getAction()),
                            s.getValue(),
                            unitService.findByName(s.getUnit())
                        ));
                    }                    
                    recipe.setSteps(newSteps);
                    Set<EquipmentProductResponse> equipmentProducts = new HashSet<EquipmentProductResponse>();
                    for (EquipmentProductRequest e : recipeRequest.getEquipmentProducts()){
                        EquipmentType eT = equipmentTypeService.findByName(e.getEquipmentType());
                        equipmentProducts.add(new EquipmentProductResponse(
                            e.getName(),
                            e.getImg(),
                            new EquipmentTypeResponse(
                                    eT.getName(),
                                    eT.getActionsToDo(),
                                    eT.getActionsDoing()
                            ),
                            e.getTags())
                        );
                    }
                    recipe.setEquipmentProducts(equipmentProducts);
                    stepRepository.saveAll(newSteps); //Will this work?
                }
                // We should redo all the equipment from the equipment request
                if(recipeRequest.getNewEquipment()){
                    Set<EquipmentResponse> equipmentsAvailable = new HashSet<EquipmentResponse>();
                    for (EquipmentRequest e : recipeRequest.getEquipmentsAvailable()){
                        EquipmentType eT = equipmentTypeService.findByName(e.getEquipmentType());
                        equipmentsAvailable.add(new EquipmentResponse(
                            e.getName(),
                            e.getImg(),
                            new EquipmentTypeResponse(
                                    eT.getName(),
                                    eT.getActionsToDo(),
                                    eT.getActionsDoing()
                            ))
                        );
                    }
                    recipe.setEquipmentsAvailable(equipmentsAvailable);
                }
                //
                recipe.setPublished(recipeRequest.getPublished()); //set published
                if(recipe.isPublished() && recipe.getSteps().size() == 0){ //really bad secure design but it works :(
                    //make sure the recipe does not have 0 steps
                    return new ResponseEntity<ApiResponse>(new ApiResponse(false, "You are unable to publish a recipe with 0 steps"),
                    HttpStatus.BAD_REQUEST);
                }
                recipeRepository.save(recipe); //update recipe
                //
                return ResponseEntity.ok(new ApiResponse(true, "Recipe was succesfully updated!"));
            }
            else{
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Unauthorized request to change settings"), 
                    HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.error("Recipe was unable to be updated.", e);
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe was unable to be updated. Error: " + e.getMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }  
    }

    @PostMapping("/{recipeID}/delete")
    public ResponseEntity<?> deleteRecipe(@PathVariable(value = "recipeID") String recipeID, @CurrentUser UserDetails currentUser) {
        try{
            User requester = userService.findByEmail(currentUser.getUsername());
            boolean isAdmin = false;
            for (Role r : requester.getRoles()){
                if(r.getName().equals("ADMIN")){
                    isAdmin = true;
                }
            }
            Recipe recipe = recipeService.findById(recipeID);
            if(recipe == null){
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe with the ID \"" + recipeID +"\" was not found."),
                HttpStatus.NOT_FOUND);
            }
            if(recipe.getAuthor().getId().equals(requester.getId()) || isAdmin){
                List<Bar> bars = barService.findByRecipe(recipe);//delete entries from all bars
                System.out.println(bars);
                for (Bar b: bars){
                    b.getRecipesAvailable().remove(recipe);
                    barRepository.save(b);
                }
                gameRepository.deleteAll(gameService.findByRecipe(recipe));//delete the game sessions with that recipe
                stepRepository.deleteAll(recipe.getSteps()); //delete the steps
                recipeRepository.delete(recipe); //delete the recipe
                return ResponseEntity.ok(new ApiResponse(true, "Recipe was succesfully deleted!"));
            }
            else{
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "An unauthorized request was made to delete this recipe"), HttpStatus.UNAUTHORIZED); 
            }
        } catch (Exception e) {
            logger.error("Recipe was unable to be deleted.", e);
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Recipe was unable to be deleted. Error: " + e.getMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        }  
    }

    @PostMapping("/{recipeID}/play")
    public ResponseEntity<?> initGame(@PathVariable(value = "recipeID") String recipeID, @CurrentUser UserDetails currentUser) {
        try{
            User player = userService.findByEmail(currentUser.getUsername());
            Recipe recipe = recipeService.findById(recipeID);
            if(recipe == null || !recipe.isPublished()){
                return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Game was unable to be initialized. Error:  Recipe with the ID \"" + recipeID +"\" was not found."),
                HttpStatus.NOT_FOUND);
            }
            //Let see if and incompleted game already exists
            Game game = gameService.findByPlayerAndRecipeAndCompleted(player, recipe, false); //omg this actually works...
            if(game != null){
                //Game exists, return that game ID
                return ResponseEntity.ok(new ApiResponse(true, game.getId())); 
            }   
            else{
            //Game doesn't exist, so lets make one
            Game newGame = new Game(
                player, 
                recipe, 
                new ArrayList<Integer>(Collections.nCopies(recipe.getSteps().size(), 0)), //size of steps array in recipe 
                false
                );
            gameRepository.save(newGame);
            return ResponseEntity.ok(new ApiResponse(true, newGame.getId())); 
        }
        } catch (Exception e) {
            logger.error("Game was unable to be initialized. Error: ", e);
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Game was unable to be initialized. Error:  " + e.getMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
        } 
    }
}