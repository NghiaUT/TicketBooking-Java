package com.nghiatr.ticket_booking.user.repository;

import com.nghiatr.ticket_booking.user.model.User;
import com.nghiatr.ticket_booking.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Tìm kiếm người dùng theo địa chỉ email.
     *
     * @param email địa chỉ email của người dùng
     * @return Optional chứa thực thể User nếu tìm thấy, ngược lại Optional rỗng
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm danh sách người dùng theo vai trò.
     *
     * @param role vai trò người dùng cần tìm
     * @return danh sách người dùng thuộc vai trò đó
     */
    List<User> findByRole(UserRole role);

    /**
     * Kiểm tra xem địa chỉ email đã tồn tại trong hệ thống hay chưa.
     *
     * @param email địa chỉ email cần kiểm tra
     * @return true nếu email đã tồn tại, ngược lại false
     */
    boolean existsByEmail(String email);

    /**
     * Tìm danh sách người dùng theo mã định danh.
     *
     * @param id định danh duy nhất của người dùng
     * @return danh sách người dùng tương ứng
     */
    List<User> id(UUID id);
}
