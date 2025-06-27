package oort.cloud.openmarket.user.service;

import oort.cloud.openmarket.auth.controller.request.SignUpRequest;
import oort.cloud.openmarket.common.exception.business.DuplicateEmailException;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.user.controller.request.AddressCreateRequest;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Address;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.repository.AddressRepository;
import oort.cloud.openmarket.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public UserDto save(SignUpRequest request){
        if(duplicateEmail(request.getEmail()))
            throw new DuplicateEmailException();

        Users savedUser = userRepository.save(
                Users.createUser(
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword()),
                        request.getUserName(),
                        request.getPhone(),
                        request.getUserRole()
                )
        );

        return UserDto.of(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getUserName(),
                savedUser.getPhone(),
                savedUser.getUserRole(),
                savedUser.getUserStatus()
        );
    }

    @Transactional
    public Long createAddress(Long userId, AddressCreateRequest request){
        Users user = userRepository.findById(userId).orElseThrow(() -> new NotFoundResourceException("조회된 유저가 없습니다."));
        Address address = Address.of(
                user,
                request.getAddress(),
                request.getAddressDetail(),
                request.getPostalCode());
        return addressRepository.save(address).getAddressId();
    }

    public Address findAddressById(Long addressId){
        return addressRepository.findById(addressId).orElseThrow(() -> new NotFoundResourceException("조회된 주소가 없습니다."));
    }

    public Users findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Users findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResourceException("조회된 유저 정보가 없습니다."));
    }

    public Users findUserEntityById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResourceException("조회된 유저 정보가 없습니다."));
    }

    public boolean duplicateEmail(String email){
        return userRepository.existsByEmail(email);
    }
}
