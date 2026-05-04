package com.app.infrastructure.routing.handlers;

import com.app.application.dto.CreateUserDto;
import com.app.application.dto.ResponseErrorDto;
import com.app.application.dto.UserDto;
import com.app.application.exception.RegistrationUserException;
import com.app.application.service.UsersService;
import com.app.infrastructure.aspect.annotations.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class UsersHandler {
    private final UsersService usersService;

    @Loggable
    @Operation(summary = "POST register user", requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateUserDto.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> register(ServerRequest serverRequest) {

        return serverRequest
                .bodyToMono(CreateUserDto.class)
                .switchIfEmpty(Mono.error(() -> new RegistrationUserException("No body found")))
                .flatMap(createUserDto -> usersService
                        .register(createUserDto)
                        .flatMap(response -> ServerResponse
                                .status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(response))));

    }

    @Loggable
    @Operation(summary = "GET all users", security = @SecurityRequirement(name = "JwtAuthToken"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getAllUsers(ServerRequest serverRequest) {

        return usersService
                .getAll()
                .collectList()
                .flatMap(usersList -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(usersList)));
    }

    @Loggable
    @Operation(summary = "GET user by username", security = @SecurityRequirement(name = "JwtAuthToken"), parameters = {@Parameter(in = ParameterIn.PATH, name = "username")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(schema = @Schema(implementation = UserDto.class), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> getByUsername(ServerRequest serverRequest) {
        return usersService.getByUsername(serverRequest.pathVariable("username"))
                .flatMap(user -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(user))
                );
    }

    @Loggable
    @Operation(summary = "POST promote user to admin", security = @SecurityRequirement(name = "JwtAuthToken"), parameters = {@Parameter(ref = "username")})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {
                    @Content(schema = @Schema(implementation = UserDto.class), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseErrorDto.class))
            })

    })
    public Mono<ServerResponse> promoteUserToAdminRole(ServerRequest serverRequest) {
        return usersService.promoteUserToAdminRole(serverRequest.pathVariable("username"))
                .flatMap(user -> ServerResponse
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(user))
                );
    }
}
