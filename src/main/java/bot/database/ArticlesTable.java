package bot.database;

import bot.outputData.articles.Article;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.ResultSet;
import java.util.LinkedList;

public class ArticlesTable
{
    static void initialize(ComboPooledDataSource dataSource)
    {
        ArticlesTable.dataSource = dataSource;
    }

    public static LinkedList<Article> getArticles(){

        return new LinkedList<>();
    }



    private static ComboPooledDataSource dataSource;            // пул коннекторов к БД

    private static final String GET_ARTICLES =            // получение всех готовых статей
            "SELECT  id, preview, reference " +
                    "FROM articles " +
                    "WHERE isReady = TRUE";
}
