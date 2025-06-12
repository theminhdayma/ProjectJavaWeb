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

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/technology")
@RequiredArgsConstructor
public class TechnologyController {

    private final TechnologyService technologyService;

    @GetMapping
    public String listTechnologies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            Model model,
            @RequestParam(required = false) String errorMessage,
            @RequestParam(required = false) String successMessage) {

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
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("successMessage", successMessage);

        return "admin/technology/technologyManager";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("technologyDto", new TechnologyDto());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", false);
        return "admin/technology/addTechnology";
    }

    @PostMapping("/add")
    public String addTechnology(
            @Valid @ModelAttribute("technologyDto") TechnologyDto technologyDto,
            BindingResult bindingResult,
            Model model) {

        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", false);

        if (bindingResult.hasErrors()) {
            return "admin/technology/addTechnology";
        }

        if (technologyService.checkNameTechnology(technologyDto.getName())) {
            model.addAttribute("errorMessage", "Tên công nghệ đã tồn tại!");
            return "admin/technology/addTechnology";
        }

        try {
            Technology technology = convertToEntity(technologyDto);
            technology.setStatus(Status.ACTIVE);
            technologyService.saveTechnology(technology);
            return "redirect:/admin/technology?successMessage=Thêm công nghệ thành công!";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi thêm công nghệ!");
            return "admin/technology/addTechnology";
        }
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Technology technology = technologyService.findTechnologyById(id);
        if (technology == null) {
            return listTechnologies(0, 5, null, model, "Không tìm thấy công nghệ!", null);
        }

        TechnologyDto technologyDto = convertToDto(technology);
        model.addAttribute("technologyDto", technologyDto);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", true);
        return "admin/technology/editTechnology";
    }

    @PostMapping("/edit/{id}")
    public String updateTechnology(
            @PathVariable int id,
            @Valid @ModelAttribute("technologyDto") TechnologyDto technologyDto,
            BindingResult bindingResult,
            Model model) {

        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", true);

        if (bindingResult.hasErrors()) {
            return "admin/technology/editTechnology";
        }

        try {
            Technology existingTechnology = technologyService.findTechnologyById(id);
            if (existingTechnology == null) {
                return "redirect:/admin/technology";
            }

            boolean isNameTaken = technologyService.checkNameTechnology(technologyDto.getName());
            if (isNameTaken && !technologyDto.getName().equalsIgnoreCase(existingTechnology.getName())) {
                model.addAttribute("errorMessage", "Tên công nghệ đã tồn tại!");
                return "admin/technology/editTechnology";
            }

            // Cập nhật thông tin
            existingTechnology.setName(technologyDto.getName());
            existingTechnology.setStatus(Status.ACTIVE);

            boolean success = technologyService.updateTechnology(existingTechnology);

            if (success) {
                return "redirect:/admin/technology?successMessage=Câp nhật công nghệ thành công!";
            } else {
                model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật công nghệ!");
                return "admin/technology/editTechnology";
            }

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật công nghệ!");
            return "admin/technology/editTechnology";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTechnology(@PathVariable("id") int id, Model model) {
        boolean deleted = technologyService.deleteTechnology(id);
        if (!deleted) {
            model.addAttribute("errorMessage", "Không thể xóa công nghệ vì đang được sử dụng.");
        } else {
            model.addAttribute("successMessage", "Xóa công nghệ thành công.");
        }
        return "redirect:/admin/technology";
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
