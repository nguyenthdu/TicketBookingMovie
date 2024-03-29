package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.SignupDto;
import com.app.TicketBookingMovie.dtos.UserDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Role;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.models.enums.ERole;
import com.app.TicketBookingMovie.repository.RoleRepository;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.security.PasswordConfig;
import com.app.TicketBookingMovie.security.UserDetailsImpl;
import com.app.TicketBookingMovie.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    //TODO:  keyword final, bạn đảm bảo rằng field userRepository không thể được thay đổi sau khi đối tượng UserServiceImpl được tạo. Điều này tăng cường độ tin cậy và giúp code dễ hiểu hơn.
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    // Inject the PasswordEncoder in the constructor
    private final PasswordConfig passwordConfig;
    JwtUtils jwtUtils;


    public UserServiceImpl(JwtUtils jwtUtils, UserRepository userRepository, ModelMapper modelMapper, RoleRepository roleRepository, PasswordConfig passwordConfig) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
        this.passwordConfig = passwordConfig;

    }
  /*TODO: constructor injection tốt hơn
  * Testability (Khả năng test): Constructor injection giúp bạn dễ dàng kiểm soát các dependency được sử dụng trong unit test. Bạn chỉ cần tạo một mock object cho UserRepository và truyền trực tiếp vào constructor của UserServiceImpl.
	Explicit Dependencies (Sự rõ ràng): Constructor injection làm cho dependencies của một class trở nên rõ ràng. Bất kỳ ai nhìn vào constructor cũng đều có thể hiểu được những dependency mà class cần để hoạt động.
	Immutability (Tính bất biến): Bằng cách sử dụng keyword final, bạn đảm bảo rằng field userRepository không thể được thay đổi sau khi đối tượng UserServiceImpl được tạo. Điều này tăng cường độ tin cậy và giúp code dễ hiểu hơn.
   */


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        return UserDetailsImpl.build(user);
    }

    @Override
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new AppException("Not found user with email: " + email, HttpStatus.NOT_FOUND));

    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException("Not found user with id: " + id, HttpStatus.NOT_FOUND));
    }

    /***
     *
     * @param page
     * @param size
     * @param code
     * @param username
     * @param phone
     * @param email
     * @return List<UserDTO>
     */
    @Override
    public List<UserDto> getAllUsersPage(Integer page,
                                         Integer size, String code, String username,
                                         String phone, String email, Long roleId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;
        if (code != null && !code.isEmpty()) {
            userPage = userRepository.findByCodeContaining(code, pageable);
        } else if (username != null && !username.isEmpty()) {
            userPage = userRepository.findByUsernameContaining(username, pageable);
        } else if (phone != null && !phone.isEmpty()) {
            userPage = userRepository.findByPhoneContaining(phone, pageable);
        } else if (email != null && !email.isEmpty()) {
            userPage = userRepository.findByEmailContaining(email, pageable);
        } else if (roleId != null && roleId > 0) {
            userPage = userRepository.findByRoleId(roleId, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        // Filter out users with role "admin"
//        List<UserDto> filteredUsers = userPage.stream()
//                .filter(user -> !user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_ADMIN))
//                .map(user -> modelMapper.map(user, UserDto.class))
//                .collect(Collectors.toList());
        return userPage.stream()
                .filter(user -> user.getRoles().stream().noneMatch(role -> role.getName() == ERole.ROLE_ADMIN))
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("Not found user with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(user, UserDto.class);
    }


    @Override
    public void deleteUser(Long id) {
        //xoa sẽ chuyển trạng thái user thành false
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("Not found user with id: " + id, HttpStatus.NOT_FOUND));
        //khong the xoa user voi role la admin
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN))) {
            throw new AppException("Can't delete user with role admin!", HttpStatus.BAD_REQUEST);
        }
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void updateUser(Long id, UserDto userDTO) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("Not found user with id: " + id, HttpStatus.NOT_FOUND));
        if (userRepository.existsByPhone(userDTO.getPhone()) && !user.getPhone().equals(userDTO.getPhone())) {
            throw new AppException("Phone is already taken!", HttpStatus.BAD_REQUEST);
        }
        if (!userDTO.getUsername().isEmpty() && !userDTO.getUsername().isBlank()) {
            user.setUsername(userDTO.getUsername());
        } else {
            user.setUsername(user.getUsername());
        }
        if (userDTO.isGender() != user.isGender()) {
            user.setGender(userDTO.isGender());
        } else {
            user.setGender(user.isGender());
        }
        if (userDTO.getBirthday() != user.getBirthday()) {
            user.setBirthday(userDTO.getBirthday());
        } else {
            user.setBirthday(user.getBirthday());
        }
        if (!userDTO.getPhone().isEmpty() && !userDTO.getPhone().isBlank()) {
            user.setPhone(userDTO.getPhone());
        } else {
            user.setPhone(user.getPhone());
        }
        userRepository.save(user);
        modelMapper.map(user, UserDto.class);
    }

    @Override
    public void updateUserProfile(String email, UserDto userDTO) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException("Not found user with email: " + email, HttpStatus.NOT_FOUND));
        if (userRepository.existsByPhone(userDTO.getPhone()) && !user.getPhone().equals(userDTO.getPhone())) {
            throw new AppException("Phone is already taken!", HttpStatus.BAD_REQUEST);
        }
        if (!userDTO.getUsername().isEmpty() && !userDTO.getUsername().isBlank()) {
            user.setUsername(userDTO.getUsername());
        } else {
            user.setUsername(user.getUsername());
        }
        if (userDTO.isGender() != user.isGender()) {
            user.setGender(userDTO.isGender());
        } else {
            user.setGender(user.isGender());
        }
        if (userDTO.getBirthday() != user.getBirthday()) {
            user.setBirthday(userDTO.getBirthday());
        } else {
            user.setBirthday(user.getBirthday());
        }
        if (!userDTO.getPhone().isEmpty() && !userDTO.getPhone().isBlank()) {
            user.setPhone(userDTO.getPhone());
        } else {
            user.setPhone(user.getPhone());
        }
        userRepository.save(user);
    }

    @Override
    public void createGuest() {
        User user = new User();
        user.setCode(randomCode());
        LocalDateTime now = LocalDateTime.now();
        user.setUsername("Khách hàng_" + now.getDayOfMonth() + now.getMonthValue() + now.getYear() + "_" + now.getHour() + now.getMinute() + now.getSecond());
        Role role = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new AppException("Error: Role is not found.", HttpStatus.NOT_FOUND));
        user.setRoles(Set.of(role));
        user.setEnabled(true);
        user.setCreatedDate(now);
        userRepository.save(user);
    }

    @Override
    public void createUserInTicket(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new AppException("Email is already taken!", HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setCode(randomCode());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordConfig.passwordEncoder()
                .encode("cinema123456"));
        Role role = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new AppException("Error: Role is not found.", HttpStatus.NOT_FOUND));
        user.setRoles(Set.of(role));
        user.setCreatedDate(LocalDateTime.now());
        user.setEnabled(true);
        userRepository.save(user);
    }

    //TODO: create user
    @Override
    public void createUser(SignupDto signupDto) {
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new AppException("Email is already taken!", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByPhone(signupDto.getPhone())) {
            throw new AppException("Phone is already taken!", HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setCode(randomCode());
        user.setUsername(signupDto.getUsername());
        user.setEmail(signupDto.getEmail());
        user.setGender(signupDto.isGender());
        user.setBirthday(signupDto.getBirthday());
        user.setPhone(signupDto.getPhone());
        Role role = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new AppException("Error: Role is not found.", HttpStatus.NOT_FOUND));
        user.setRoles(Set.of(role));
//        user.setCreatedDate(LocalDateTime.now());
        user.setEnabled(true);
        user.setCreatedDate(LocalDateTime.now());
        user.setPassword(passwordConfig.passwordEncoder()
                .encode(signupDto.getPassword()));
        userRepository.save(user);
    }

    public String randomCode() {
        return "KH" + LocalDateTime.now().getNano();
    }

    public String randomCodeMor() {
        return "MO" + LocalDateTime.now().getNano();
    }


    @Override
    public void createMor(SignupDto signupDto) {
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new AppException("Email is already taken!", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByPhone(signupDto.getPhone())) {
            throw new AppException("Phone is already taken!", HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setCode(randomCodeMor());
        user.setUsername(signupDto.getUsername());
        user.setEmail(signupDto.getEmail());
        user.setGender(signupDto.isGender());
        LocalDate birthday = LocalDate.parse("2000-01-01");
        user.setBirthday(birthday);
        Role role = roleRepository.findByName(ERole.ROLE_MODERATOR).orElseThrow(() -> new AppException("Error: Role is not found.", HttpStatus.NOT_FOUND));
        user.setRoles(Set.of(role));
//        user.setCreatedDate(LocalDateTime.now());
        user.setPhone("0120000000");
        user.setEnabled(true);
        user.setCreatedDate(LocalDateTime.now());
        user.setPassword(passwordConfig.passwordEncoder()
                .encode(signupDto.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void createRoles() {
        if (roleRepository.findByName(ERole.valueOf("ROLE_ADMIN")).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
        if (roleRepository.findByName(ERole.valueOf("ROLE_USER")).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));

        }
        if (roleRepository.findByName(ERole.valueOf("ROLE_MODERATOR")).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_MODERATOR));
        }
    }

    @Override
    public void createAdmin() {
        User user = new User();
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            Role role = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new AppException("Error: Role is not found.", HttpStatus.NOT_FOUND));
            user.setRoles(Set.of(role));
            user.setUsername("admin");
            user.setEmail("admin@gmail.com");
            user.setGender(true);
            LocalDate birthday = LocalDate.parse("2000-01-01");
            user.setBirthday(birthday);
            user.setPhone("0120000001");
            user.setCode("ADMIN123456789");
//            user.setCreatedDate(LocalDateTime.now());
            user.setEnabled(true);
            user.setPassword(passwordConfig.passwordEncoder()
                    .encode("admin123456"));
            userRepository.save(user);
        }
    }

    @Override
    public long countUsers(String code, String username, String phone, String email, Long roleId) {
        if (code != null && !code.isEmpty()) {
            return userRepository.countByCodeContaining(code);
        } else if (username != null && !username.isEmpty()) {
            return userRepository.countByUsernameContaining(username);
        } else if (phone != null && !phone.isEmpty()) {
            return userRepository.countByPhoneContaining(phone);
        } else if (email != null && !email.isEmpty()) {
            return userRepository.countByEmailContaining(email);
        } else if (roleId != null && roleId != 0) {
            return userRepository.countByRoleId(roleId);
        } else {
            return userRepository.count();
        }
    }


}


