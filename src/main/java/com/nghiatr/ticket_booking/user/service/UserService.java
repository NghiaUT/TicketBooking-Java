package com.nghiatr.ticket_booking.user.service;

import com.nghiatr.ticket_booking.user.model.*;
import com.nghiatr.ticket_booking.user.repository.AdminRepository;
import com.nghiatr.ticket_booking.user.repository.CustomerRepository;
import com.nghiatr.ticket_booking.user.repository.OrganizerRepository;
import com.nghiatr.ticket_booking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final OrganizerRepository organizerRepository;
    private final AdminRepository adminRepository;

    /**
     * Tạo mới người dùng với mật khẩu được mã hóa và lưu vào cơ sở dữ liệu.
     *
     * @param name họ và tên người dùng
     * @param email địa chỉ email người dùng
     * @param password mật khẩu dạng văn bản gốc
     * @param passwordEncoder bộ mã hóa mật khẩu
     * @param role vai trò của người dùng
     * @return thực thể User vừa tạo
     */
    public User createUser(
            String name,
            String email,
            String password,
            PasswordEncoder passwordEncoder,
            UserRole role
    ) {
        User user =  User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    /**
     * Tạo mới bản ghi khách hàng Customer liên kết với tài khoản người dùng.
     *
     * @param user thực thể người dùng tương ứng
     * @return thực thể Customer vừa tạo
     */
    public Customer createCustomer(
            User user
    ) {
        Customer customer = Customer.builder()
                .user(user)
                .build();

        return customerRepository.save(customer);
    }

    /**
     * Tạo mới bản ghi ban tổ chức Organizer liên kết với tài khoản người dùng.
     *
     * @param user thực thể người dùng tương ứng
     * @return thực thể Organizer vừa tạo
     */
    public Organizer createOrganizer(
            User user
    ) {
        Organizer organizer = Organizer.builder()
                .user(user)
                .build();

        return  organizerRepository.save(organizer);
    }

    /**
     * Tạo mới bản ghi quản trị viên Admin liên kết với tài khoản người dùng.
     *
     * @param user thực thể người dùng tương ứng
     * @return thực thể Admin vừa tạo
     */
    public Admin createAdmin(
            User user
    ) {
        Admin admin = Admin.builder()
                .user(user)
                .build();

        return  adminRepository.save(admin);
    }
}
