package io.github.iamzaid.ipldashboard.data;

import io.github.iamzaid.ipldashboard.model.Match;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final String[] FIELD_NAMES = new String[]{
    "id",
    "city",
    "date",
    "player_of_match",
    "venue",
    "neutral_venue",
    "team1",
    "team2",
    "toss_winner",
    "toss_decision",
    "winner",
    "result",
    "result_margin",
    "eliminator",
    "method",
    "umpire1",
    "umpire2"
    };

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    //Note this uses a memory based database provided by @EnableBatchProcessing
    //How it works
    //We define one bean which specifies where to read the data from, in our case .csv file
    //Then second bean processes the data converts the data from one form to other
    //Third bean is the writer that writes to the in memory database

    @Bean
    //Reader Bean
    public FlatFileItemReader<MatchInput> reader() {
        return new FlatFileItemReaderBuilder<MatchInput>()
                .name("MatchItemReader")
                .resource(new ClassPathResource("match-data.csv"))//File name where it has to read from
                .delimited()
                .names(FIELD_NAMES)//String array of fields that it needs to read from the csv file
                .fieldSetMapper(new BeanWrapperFieldSetMapper<MatchInput>() {{
                    setTargetType(MatchInput.class);//Setting the mapping of the data we are reading form .csv
                    //Here mapping is .csv -> MatchInput.java
                }})
                .build();
    }

    @Bean
    //Processing Bean
    public MatchDataProcessor processor() {
        return new MatchDataProcessor();
        //We have written the logic for this processor in our MatchDataProcessor.java class
    }

    @Bean
    //This Bean writes to a database
    public JdbcBatchItemWriter<Match> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Match>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO match (id, city, date, player_of_match, venue, team1, team2, toss_winner, toss_decision, match_winner, result, result_margin, umpire1, umpire2) "
                        + " VALUES (:id, :city, :date, :playerOfMatch, :venue, :team1, :team2, :tossWinner, :tossDecision, :matchWinner, :result, :resultMargin, :umpire1, :umpire2)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    //Listener runs when the job is completed and it takes the parameter Step which is a Bean defined below
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory
                .get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    //The Step bean does all 3 processes : read, write and process
    public Step step1(JdbcBatchItemWriter<Match> writer) {
        return stepBuilderFactory
                .get("step1")
                .<MatchInput, Match> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }

}




