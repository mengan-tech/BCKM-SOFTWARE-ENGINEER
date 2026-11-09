package edu.itc.shop.support;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

@Component 
public class QueryCounter {
    private final Statistics stats;
    
    public QueryCounter(EntityManagerFactory emf){
        this.stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
    }

    public void reset(){
        stats.clear();
    }

    public long count(){
        return stats.getPrepareStatementCount();
    }

    public long inserts(){
        return stats.getEntityInsertCount();
    }
}
