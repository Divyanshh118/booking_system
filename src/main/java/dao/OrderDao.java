package dao;

import model.Order;
import java.sql.SQLException;
import java.util.List;

public interface OrderDao {

    void saveOrder(Order order, int userId) throws SQLException;


    List<Order> getOrdersByUserId(int userId) throws SQLException;


    List<Order> getAllOrders() throws SQLException;
}