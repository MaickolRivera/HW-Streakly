namespace Streakly.Application.Services;

using Streakly.Application.DTOs;
using Streakly.Application.Exceptions;
using Streakly.Application.Interfaces;
using Streakly.Domain.Entities;
using Streakly.Domain.Exceptions;

public class AuthService : IAuthService
{
    private const int MinPasswordLength = 8;

    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher _passwordHasher;
    private readonly ITokenGenerator _tokenGenerator;

    public AuthService(IUserRepository userRepository, IPasswordHasher passwordHasher, ITokenGenerator tokenGenerator)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
        _tokenGenerator = tokenGenerator;
    }

    public async Task<AuthResponse> RegisterAsync(RegisterRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Password) || request.Password.Length < MinPasswordLength)
            throw new DomainException($"La contraseña debe tener al menos {MinPasswordLength} caracteres.");
        if (string.IsNullOrWhiteSpace(request.Email))
            throw new DomainException("El correo electrónico no es válido.");
        if (await _userRepository.ExistsByEmailAsync(request.Email))
            throw new ConflictException("Ya existe una cuenta con ese correo.");

        var user = new User(request.Email, _passwordHasher.Hash(request.Password));
        await _userRepository.AddAsync(user);
        return BuildResponse(user);
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Email) || string.IsNullOrEmpty(request.Password))
            throw new UnauthorizedException("Correo o contraseña incorrectos.");

        var user = await _userRepository.GetByEmailAsync(request.Email);
        if (user == null || !_passwordHasher.Verify(request.Password, user.PasswordHash))
            throw new UnauthorizedException("Correo o contraseña incorrectos.");

        return BuildResponse(user);
    }

    public async Task<UserResponse> GetCurrentUserAsync(Guid userId)
    {
        var user = await _userRepository.GetByIdAsync(userId)
            ?? throw new NotFoundException("El usuario no existe.");
        return new UserResponse(user.Id, user.Email, user.CreatedAt);
    }

    private AuthResponse BuildResponse(User user)
    {
        var (token, expiresAt) = _tokenGenerator.Generate(user);
        return new AuthResponse(token, expiresAt, new UserResponse(user.Id, user.Email, user.CreatedAt));
    }
}
