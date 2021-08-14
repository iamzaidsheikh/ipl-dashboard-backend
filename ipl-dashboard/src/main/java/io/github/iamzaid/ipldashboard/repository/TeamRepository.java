package io.github.iamzaid.ipldashboard.repository;

import io.github.iamzaid.ipldashboard.model.Team;
import org.springframework.data.repository.CrudRepository;

public interface TeamRepository extends CrudRepository<Team, Long>{
    //It is Generic type which has 2 params
    //1 : Thing which we are fetching : Here team
    //2 : The id type : Here our id is of type long
    //We can define methods that perform the action according to their name
    //Spring JPA will look at the method name to find out what the implementation should be

    Team findByTeamName(String teamName);//JPA writes the code to query the table and find an instance of team with team name teamName

}

