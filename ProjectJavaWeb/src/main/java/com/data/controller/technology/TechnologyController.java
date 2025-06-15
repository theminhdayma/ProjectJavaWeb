package com.data.controller.technology;

import com.data.dto.TechnologyDto;
import com.data.entity.Technology;
import com.data.entity.enums.Status;
import com.data.service.technology.TechnologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

import static com.data.utils.PaginationUtil.*;

@Controller
@RequestMapping("/admin/technology")
@RequiredArgsConstructor
public class TechnologyController {

    private final TechnologyService technologyService;

    @GetMapping
    public String listTechnologies(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            Model model) {

        List<Technology> technologies;
        long totalItems;
        if (search != null && !search.trim().isEmpty()) {
            technologies = technologyService.findTechnologiesByName(search.trim(), page, size);
            totalItems = technologyService.countTechnologiesByName(search.trim());
            model.addAttribute("search", search);
        } else {
            technologies = technologyService.findAllTechnologies(page, size);
            totalItems = technologyService.countAllTechnologies();
        }

        int totalPages = (int) Math.ceil((double) totalItems / size);

        model.addAttribute("technologies", technologies);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/technology/technologyManager";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("technologyDto", new TechnologyDto());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", false);
        return "admin/technology/formTechnology";
    }

    @PostMapping("/add")
    public String addTechnology(
            @Valid @ModelAttribute("technologyDto") TechnologyDto technologyDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", false);

        if (bindingResult.hasErrors()) {
            return "admin/technology/formTechnology";
        }

        if (technologyService.checkNameTechnology(technologyDto.getName())) {
            model.addAttribute("errorMessage", "Tên công nghệ đã tồn tại!");
            return "admin/technology/formTechnology";
        }

        try {
            Technology technology = convertToEntity(technologyDto);
            technology.setStatus(Status.ACTIVE);
            technologyService.saveTechnology(technology);

            // Thêm thông báo thành công với loại action
            redirectAttributes.addFlashAttribute("successMessage", "Thêm công nghệ '" + technologyDto.getName() + "' thành công!");
            redirectAttributes.addFlashAttribute("actionType", "add");
            redirectAttributes.addFlashAttribute("techName", technologyDto.getName());

            return "redirect:/admin/technology";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi thêm công nghệ!");
            return "admin/technology/formTechnology";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Technology technology = technologyService.findTechnologyById(id);
        if (technology == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy công nghệ!");
            redirectAttributes.addFlashAttribute("actionType", "error");
            return "redirect:/admin/technology";
        }

        TechnologyDto technologyDto = convertToDto(technology);
        model.addAttribute("technologyDto", technologyDto);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", true);
        return "admin/technology/formTechnology";
    }

    @PostMapping("/edit/{id}")
    public String updateTechnology(
            @PathVariable int id,
            @Valid @ModelAttribute("technologyDto") TechnologyDto technologyDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", true);

        if (bindingResult.hasErrors()) {
            return "admin/technology/formTechnology";
        }

        try {
            Technology existingTechnology = technologyService.findTechnologyById(id);
            if (existingTechnology == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy công nghệ!");
                redirectAttributes.addFlashAttribute("actionType", "error");
                return "redirect:/admin/technology";
            }

            boolean isNameTaken = technologyService.checkNameTechnology(technologyDto.getName());
            if (isNameTaken && !technologyDto.getName().equalsIgnoreCase(existingTechnology.getName())) {
                model.addAttribute("errorMessage", "Tên công nghệ đã tồn tại!");
                return "admin/technology/formTechnology";
            }

            String oldName = existingTechnology.getName();
            existingTechnology.setName(technologyDto.getName());
            existingTechnology.setStatus(Status.ACTIVE);

            boolean success = technologyService.updateTechnology(existingTechnology);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật công nghệ '" + technologyDto.getName() + "' thành công!");
                redirectAttributes.addFlashAttribute("actionType", "edit");
                redirectAttributes.addFlashAttribute("techName", technologyDto.getName());
                redirectAttributes.addFlashAttribute("oldName", oldName);
                return "redirect:/admin/technology";
            } else {
                model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật công nghệ!");
                return "admin/technology/formTechnology";
            }

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật công nghệ!");
            return "admin/technology/formTechnology";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTechnology(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            Technology technology = technologyService.findTechnologyById(id);
            if (technology == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy công nghệ!");
                redirectAttributes.addFlashAttribute("actionType", "error");
                return "redirect:/admin/technology";
            }

            String techName = technology.getName();
            boolean success = technologyService.deleteTechnology(id);

            if (success) {
                Technology afterDelete = technologyService.findTechnologyById(id);

                if (afterDelete == null) {
                    redirectAttributes.addFlashAttribute("successMessage", "Xóa công nghệ '" + techName + "' thành công!");
                    redirectAttributes.addFlashAttribute("actionType", "delete");
                    redirectAttributes.addFlashAttribute("techName", techName);
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", "Công nghệ '" + techName + "' đã được đặt thành không hoạt động!");
                    redirectAttributes.addFlashAttribute("actionType", "deactivate");
                    redirectAttributes.addFlashAttribute("techName", techName);
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa công nghệ!");
                redirectAttributes.addFlashAttribute("actionType", "error");
            }

            return "redirect:/admin/technology";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa công nghệ!");
            redirectAttributes.addFlashAttribute("actionType", "error");
            return "redirect:/admin/technology";
        }
    }

    private TechnologyDto convertToDto(Technology technology) {
        TechnologyDto dto = new TechnologyDto();
        dto.setId(technology.getId());
        dto.setName(technology.getName());
        dto.setStatus(technology.getStatus());
        dto.setCandidates(technology.getCandidates());
        dto.setPositions(technology.getPositions());
        return dto;
    }

    private Technology convertToEntity(TechnologyDto dto) {
        Technology technology = new Technology();
        technology.setId(dto.getId());
        technology.setName(dto.getName());
        technology.setStatus(dto.getStatus());
        technology.setCandidates(dto.getCandidates());
        technology.setPositions(dto.getPositions());
        return technology;
    }
}
