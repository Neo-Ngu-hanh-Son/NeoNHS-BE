package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - Users", description = "Admin APIs for managing users (requires ADMIN role)")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService adminUserService;

    @GetMapping
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIR, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) UserRole role,
            @RequestParam(value = "isBanned", required = false) Boolean isBanned,
            @RequestParam(value = "deleted", required = false) Boolean deleted,
            @RequestParam(value = "includeDeleted", defaultValue = "false", required = false) Boolean includeDeleted
    ) {
        return ApiResponse.success(adminUserService.getAllUsersWithPagination(
                page, size, sortBy, sortDir, search, role, isBanned, deleted, includeDeleted));
    }

    @PatchMapping("/{id}/toggle-ban")
    public ApiResponse<String> toggleBan(@PathVariable UUID id) {
        adminUserService.toggleBanUser(id);
        return ApiResponse.success("User status updated successfully");
    }
}
