package mvc.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import mvc.database.DBconnection;

public class OrderDAO {

	private static OrderDAO instance;
	
	private OrderDAO() {
		
	}
	
	public static OrderDAO getInstance() {
		if (instance == null)
			instance = new OrderDAO();
		return instance;
	}
	
	public void clearOrderData (String orderNo) {
		// �ֹ���ȣ �������� �ֹ������� ����. �ߺ� ��� ������
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			String sql = "DELETE FROM order_data WHERE orderNo = ? ";
			
			conn  = DBconnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, orderNo);
			pstmt.executeUpdate();
		} catch (Exception ex) {
			System.out.println("ClearOrderData() error: " + ex);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}
		}
	}
	
	public boolean insertOrderData(OrderDataDTO dto) {
		int flag = 0;
		String sql = "INSERT INTO order_data VALUES (null, ?, ?, ?, ?, ?, ?, ?)";
		
		try (Connection conn = DBconnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, dto.getOrderNo());
			pstmt.setInt(2, dto.getCartId());
			pstmt.setString(3, dto.getP_id());
			pstmt.setString(4, dto.getP_name());
			pstmt.setInt(5, dto.getUnitPrice());
			pstmt.setInt(6, dto.getCnt());
			pstmt.setInt(7, dto.getSumPrice());
			flag = pstmt.executeUpdate();
		} catch (Exception ex) {
			System.out.println("insertOrderData() error : " + ex);
		}
		return flag != 0;
	}

	
	public ArrayList<OrderDataDTO> selectAllOrderData(String orderNo) {
		ArrayList<OrderDataDTO> dtos = new ArrayList<>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT * from order_data WHERE orderNo = ? ";
		
		try {
			conn  = DBconnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, orderNo);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				OrderDataDTO dto = new OrderDataDTO();
				dto.setNum(rs.getInt("num"));
				dto.setOrderNo(rs.getString("orderNo"));
				dto.setCartId(rs.getInt("cartId"));
				dto.setP_id(rs.getString("p_id"));
				dto.setP_name(rs.getString("p_name"));
				dto.setUnitPrice(rs.getInt("unitPrice"));
				dto.setCnt(rs.getInt("cnt"));
				dto.setSumPrice(rs.getInt("sumPrice"));
				dtos.add(dto);
			}
		} catch (Exception ex) {
			System.out.println("SelectAllOrderData() ���� : " + ex);
		}
		
		return dtos;
	}
	
	
	public int getTotalPrice(String orderNo) {
		// 3. �ֹ��� �ݾ��� ������ ��
        int totalPrice = 0;
        String sql = "SELECT SUM(sumPrice) FROM order_data WHERE orderNo = '" + orderNo + "'";
        try(Connection conn = DBconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();) {
            if(rs.next()) {
                totalPrice = rs.getInt(1);
            }
        } catch (Exception ex) {
            System.out.println("getTotalPrice() ���� : " + ex);
        }
        return totalPrice;
	}
	
	public void clearOrderInfo(String orderNo) {
		// �ֹ���ȣ �������� �ֹ� ���� ������ ����. �ߺ� ��� ����
		String sql = "DELETE FROM order_info WHERE orderNo = ?";
		try (Connection conn = DBconnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, orderNo);
			pstmt.executeUpdate();
		} catch (Exception ex) {
			System.out.println("clearOrdedrInfo() ���� : " + ex);
		}
	}
	
	public boolean insertOrderInfo(OrderInfoDTO dto) {
		int flag = 0;
		String sql = "INSERT INTO order_info VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)";
		
		try (Connection conn = DBconnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, dto.getOrderNo());
			pstmt.setString(2, dto.getMemberId());
			pstmt.setString(3, dto.getOrderName());
			pstmt.setString(4, dto.getOrderTel());
			pstmt.setString(5, dto.getOrderEmail());
			pstmt.setString(6, dto.getReceiveName());
			pstmt.setString(7, dto.getReceiveTel());
			pstmt.setString(8, dto.getReceiveAddress());
			pstmt.setInt(9, dto.getPayAmount());
			pstmt.setString(10, dto.getPayMethod());
			pstmt.setString(11, dto.getCarryNo());
			pstmt.setString(12, "orderFail");
			pstmt.setString(13, dto.getDatePay());
			pstmt.setString(14, dto.getDateCarry());
			pstmt.setString(15, dto.getDateDone());
			flag = pstmt.executeUpdate();
		} catch(Exception ex) {
			System.out.println("insertOrderInfo() ���� : " + ex);
		}
		return flag != 0;
	}
	
	public OrderInfoDTO getOrderInfo(String orderNo) {
		OrderInfoDTO dto = new OrderInfoDTO();
		String sql = "SELECT * FROM order_info WHERE orderNo = '" + orderNo + "'";
		try (Connection connection = DBconnection.getConnection();
			PreparedStatement pstmt = connection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();) {
			if (rs.next()) {
				dto.setOrderNo(rs.getString(1));
				dto.setMemberId(rs.getString(2));
				dto.setOrderName(rs.getString(3));
				dto.setOrderTel(rs.getString(4));
				dto.setOrderEmail(rs.getString(5));
				dto.setReceiveName(rs.getString(6));
				dto.setReceiveTel(rs.getString(7));
				dto.setReceiveAddress(rs.getString(8));
				dto.setPayAmount(rs.getInt(9));
				dto.setPayMethod(rs.getString(10));
				dto.setCarryNo(rs.getString(11));
				dto.setOrderStep(rs.getString(12));
				dto.setDateOrder(rs.getString(13));
				dto.setDatePay(rs.getString(14));
				dto.setDateCarry(rs.getString(15));
				dto.setDateDone(rs.getString(16));
			}
		} catch(Exception ex) {
			System.out.println("getOrderInfo() ���� : " + ex);
		}
		return dto;
	}
	
	public String getOrderProductName(String orderNo) {
		String orderProductName = null;
		int orderProductCnt = 0;
		
		String sql = "SELECT * FROM order_data WHERE orderNo = '" + orderNo + "'";
		try (Connection conn = DBconnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();) {
			while (rs.next()) {
				if (orderProductCnt == 0) {
					orderProductName = rs.getString("p_name");
				}
				orderProductCnt++;
			}
			orderProductName += "�� " + (orderProductCnt - 1) + "��"; 
		} catch(Exception ex) {
			System.out.println();
		}
		
		return orderProductName;
	}
	
	public boolean updateOrderInfoWhenProcessSuccess(OrderInfoDTO dto) {
		// �����ÿ� �ֹ� ���� ������Ʈ 
		int flag = 0;
		String sql = "UPDATE order_info SET payMethod = ?, orderStep = ?, datePay = now() WHERE orderNo = ?";
		
		try (Connection conn = DBconnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, dto.getPayMethod());
			pstmt.setString(2, dto.getOrderStep());
			pstmt.setString(3, dto.getOrderNo());
			flag = pstmt.executeUpdate();
		} catch(Exception ex) {
			System.out.println("updateOrderInfoWhenProcessSuucess() ���� : " + ex);
		}
		return flag == 1;
	}
	

}
