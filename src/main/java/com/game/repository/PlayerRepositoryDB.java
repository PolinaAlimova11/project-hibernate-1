package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.mysql.jdbc.Driver");
        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "my-sql-pass");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, "true");

        this.sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(com.game.entity.Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        int offset = pageSize * pageNumber;
        try(Session session = sessionFactory.openSession()) {
            Query<Player> query = session.createNativeQuery("select * from player limit :off, :limit", Player.class);
            query.setParameter("off", offset);
            query.setParameter("limit", pageSize);
            return query.getResultList();
        }
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()){
            Query<Long> query =  session.createNamedQuery("Player_getCountAll", Long.class);
            return query.uniqueResult().intValue();

        }
    }

    @Override
    public Player save(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.save(player);
                transaction.commit();
                return player;
            } catch (Exception e) {
                transaction.rollback();
                throw  new RuntimeException();
            }
        }
    }

    @Override
    public Player update(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.update(player);
                transaction.commit();
                return player;
            } catch (Exception e) {
                transaction.rollback();
                throw  new RuntimeException();
            }
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            Query<Player> query = session.createQuery("select p from Player p where p.id = :id", Player.class);
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public void delete(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.delete(player);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw  new RuntimeException();
            }
        }

    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}