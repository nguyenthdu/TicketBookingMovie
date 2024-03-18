package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.UserDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    //TODO:  keyword final, bạn đảm bảo rằng field userRepository không thể được thay đổi sau khi đối tượng UserServiceImpl được tạo. Điều này tăng cường độ tin cậy và giúp code dễ hiểu hơn.
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
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
                                         String phone, String email) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;
        if (code != null && code.isEmpty()) {
            userPage = userRepository.findByCodeContaining(code, pageable);
        } else if (username != null && !username.isEmpty()) {
            userPage = userRepository.findByUsernameContaining(username, pageable);
        } else if (phone != null && !phone.isEmpty()) {
            userPage = userRepository.findByPhoneContaining(phone, pageable);
        } else if (email != null && !email.isEmpty()) {
            userPage = userRepository.findByEmailContaining(email, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        List<UserDto> userDtos = userPage.getContent().stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
        return userDtos;
    }





    @Override
    public void deleteUser(Long id) {
        //xoa sẽ chuyển trạng thái user thành false
        Optional<User> user = userRepository.findById(id);
        user.get().setEnabled(false);
        userRepository.save(user.get());
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDTO) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("Not found user with id: " + id, HttpStatus.NOT_FOUND));
        if (userRepository.existsByPhone(userDTO.getPhone()) && !user.getPhone().equals(userDTO.getPhone())) {
            throw new AppException("Phone is already taken!", HttpStatus.BAD_REQUEST);
        }
        user.setUsername(userDTO.getUsername());
        user.setGender(userDTO.isGender());
        user.setBirthday(userDTO.getBirthday());
        user.setPhone(userDTO.getPhone());
        userRepository.save(user);
        return modelMapper.map(user, UserDto.class);
    }

    public String randomCode() {
        Random random = new Random();
        String code;
        int number = random.nextInt(1000);
        code = "KH" + System.currentTimeMillis() + number;
        return code;
    }
}


