package com.hotelbooking.Security;

import com.hotelbooking.Entities.UserEntity;
import com.hotelbooking.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUserNameIgnoreCase(username).orElseThrow(() ->
                new UsernameNotFoundException("Không tìm thấy user: " + username));

        return new CustomUserDetails(user);
    }
}
