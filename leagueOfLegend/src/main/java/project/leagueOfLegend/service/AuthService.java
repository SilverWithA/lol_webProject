package project.leagueOfLegend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.leagueOfLegend.dto.*;
import project.leagueOfLegend.entity.User;
import project.leagueOfLegend.entity.WidgetOne;
import project.leagueOfLegend.entity.WidgetTwo;
import project.leagueOfLegend.repository.UserRepository;
import project.leagueOfLegend.repository.WidgetOneRepository;
import project.leagueOfLegend.repository.WidgetTwoRepository;
import project.leagueOfLegend.security.TokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final WidgetOneRepository widgetOneRepository;
    private final WidgetTwoRepository widgetTwoRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseDto<?> signUp(SignUpDto dto) {
        String userId = dto.getUserId();
        String userPassword = dto.getUserPassword();


        String userPasswordCheck = dto.getUserPasswordCheck();

        // email 중복 확인
        try {
            if(userRepository.existsById(userId))
                return ResponseDto.setFailed("Existed Id");
        }catch (Exception e) {
            return ResponseDto.setFailed("Data Bass Error!");
        }


        // 비밀번호가 서로 다르면 failled response 반환
        if (!userPassword.equals(userPasswordCheck))
            return ResponseDto.setFailed("Password does not matched");

        //UserEntity 생성
        User user = new User(dto);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userPassword);
        user.setUserPassword(encodedPassword);
        // UserRepository를 이용해서 데이터베이스에 Entity 저장
        try {
            userRepository.save(user);

            User u = userRepository.findByUserId(user.getUserId());
            WidgetOne widgetOne = new WidgetOne();
            WidgetTwo widgetTwo = new WidgetTwo();
            widgetOne.setUser(u);
            widgetTwo.setUser(u);
            widgetOneRepository.save(widgetOne);
            widgetTwoRepository.save(widgetTwo);
        }catch (Exception e) {
            return ResponseDto.setFailed("Data Base Error");
        }


        return ResponseDto.setSuccess("Sign Up Success!!", null);
    }
    public ResponseDto<SignInResponseDto> signIn(SignInDto dto) {
        String userId = dto.getUserId();
        String userPassword = dto.getUserPassword();

        User user = null;
        WidgetOne widgetOne = null;
        WidgetTwo widgetTwo = null;
        try {
             user = userRepository.findByUserId(userId);
             // 잘못된 아이디
             if (user == null) return ResponseDto.setFailed("로그인 실패");
             // 잘못된 패스워드
            if (!passwordEncoder.matches(userPassword, user.getUserPassword()))
                return ResponseDto.setFailed("로그인 실패");
            }catch (Exception Error) {
            return ResponseDto.setFailed("Database Error");
        }

        user.setUserPassword("");

        String token = tokenProvider.create(userId);
        int exprTime = 3600000;

        widgetOne = this.widgetOneRepository.findByUser(user);
        widgetTwo = this.widgetTwoRepository.findByUser(user);
        System.out.println(widgetOne);
        System.out.println(widgetTwo);

        SignInResponseDto signInResponseDto = new SignInResponseDto(token, exprTime, user, widgetOne, widgetTwo);
        return ResponseDto.setSuccess("로그인 성공", signInResponseDto);
        }
    }


