package com.nghiatr.ticket_booking.user.model;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/* Hold the current user information perform the specific request.*/
public class CustomUserDetails implements UserDetails {
    private final User user;

    /**
     * Khởi tạo đối tượng CustomUserDetails bọc quanh thực thể User.
     *
     * @param user thực thể người dùng
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Lấy tên đăng nhập (email) của người dùng.
     *
     * @return email của người dùng
     */
    @Override
    @NullMarked
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Lấy mật khẩu đã mã hóa của người dùng.
     *
     * @return mật khẩu đã được băm
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Lấy danh sách các quyền hạn được cấp cho người dùng theo vai trò.
     *
     * @return danh sách quyền GrantedAuthority
     */
    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                )
        );
    }

    /**
     * Kiểm tra tài khoản có đang hoạt động hay không.
     *
     * @return true nếu tài khoản chưa bị xóa, ngược lại false
     */
    @Override
    public boolean isEnabled() {
        return !this.user.isDeleted();
    }

    /**
     * Lấy mã định danh duy nhất của người dùng.
     *
     * @return UUID của người dùng
     */
    public UUID getUserId() {
        return this.user.getId();
    }

    /**
     * Lấy thực thể User gốc được bọc bên trong.
     *
     * @return thực thể User
     */
    public User getUser() {
        return this.user;
    }
}
