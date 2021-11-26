
package lu.my.mall.controller.shopman;

import lu.my.mall.common.MallCategoryLevelEnum;
import lu.my.mall.common.ServiceResultEnum;
import lu.my.mall.controller.shopman.aspect.shopmanAction;
import lu.my.mall.entity.GoodsCategory;
import lu.my.mall.service.MallCategoryService;
import lu.my.mall.util.PageQueryUtil;
import lu.my.mall.util.Result;
import lu.my.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**

 */
@Controller
@RequestMapping("/shopman")
public class shopmanCategoryController {

    @Resource
    private MallCategoryService MallCategoryService;


    @GetMapping("/categories")
    public String categoriesPage(HttpServletRequest request, @RequestParam("categoryLevel") Byte categoryLevel, @RequestParam("parentId") Long parentId, @RequestParam("backParentId") Long backParentId) {
        if (categoryLevel == null || categoryLevel < 1 || categoryLevel > 3) {
            return "error/error_5xx";
        }
        request.setAttribute("path", "newbee_mall_category");
        request.setAttribute("parentId", parentId);
        request.setAttribute("backParentId", backParentId);
        request.setAttribute("categoryLevel", categoryLevel);
        return "shopman/newbee_mall_category";
    }

    /**
     * 列表
     */
    @shopmanAction(value = "查看商品分类")
    @RequestMapping(value = "/categories/list", method = RequestMethod.GET)
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit")) || StringUtils.isEmpty(params.get("categoryLevel")) || StringUtils.isEmpty(params.get("parentId"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(MallCategoryService.getCategorisPage(pageUtil));
    }

    /**
     * 列表
     */
    @shopmanAction(value = "查看商品分类")
    @RequestMapping(value = "/categories/listForSelect", method = RequestMethod.GET)
    @ResponseBody
    public Result listForSelect(@RequestParam("categoryId") Long categoryId) {
        if (categoryId == null || categoryId < 1) {
            return ResultGenerator.genFailResult("缺少参数！");
        }
        GoodsCategory category = MallCategoryService.getGoodsCategoryById(categoryId);
        //既不是一级分类也不是二级分类则为不返回数据
        if (category == null || category.getCategoryLevel() == MallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        Map categoryResult = new HashMap(4);
        if (category.getCategoryLevel() == MallCategoryLevelEnum.LEVEL_ONE.getLevel()) {
            //如果是一级分类则返回当前一级分类下的所有二级分类，以及二级分类列表中第一条数据下的所有三级分类列表
            //查询一级分类列表中第一个实体的所有二级分类
            List<GoodsCategory> secondLevelCategories = MallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(categoryId), MallCategoryLevelEnum.LEVEL_TWO.getLevel());
            if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                //查询二级分类列表中第一个实体的所有三级分类
                List<GoodsCategory> thirdLevelCategories = MallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(secondLevelCategories.get(0).getCategoryId()), MallCategoryLevelEnum.LEVEL_THREE.getLevel());
                categoryResult.put("secondLevelCategories", secondLevelCategories);
                categoryResult.put("thirdLevelCategories", thirdLevelCategories);
            }
        }
        if (category.getCategoryLevel() == MallCategoryLevelEnum.LEVEL_TWO.getLevel()) {
            //如果是二级分类则返回当前分类下的所有三级分类列表
            List<GoodsCategory> thirdLevelCategories = MallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(categoryId), MallCategoryLevelEnum.LEVEL_THREE.getLevel());
            categoryResult.put("thirdLevelCategories", thirdLevelCategories);
        }
        return ResultGenerator.genSuccessResult(categoryResult);
    }

    /**
     * 添加
     */
    @shopmanAction(value = "添加商品分类")
    @RequestMapping(value = "/categories/save", method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody GoodsCategory goodsCategory) {
        if (Objects.isNull(goodsCategory.getCategoryLevel())
                || StringUtils.isEmpty(goodsCategory.getCategoryName())
                || Objects.isNull(goodsCategory.getParentId())
                || Objects.isNull(goodsCategory.getCategoryRank())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = MallCategoryService.saveCategory(goodsCategory);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }


    /**
     * 修改
     */
    @shopmanAction(value = "修改商品分类")
    @RequestMapping(value = "/categories/update", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody GoodsCategory goodsCategory) {
        if (Objects.isNull(goodsCategory.getCategoryId())
                || Objects.isNull(goodsCategory.getCategoryLevel())
                || StringUtils.isEmpty(goodsCategory.getCategoryName())
                || Objects.isNull(goodsCategory.getParentId())
                || Objects.isNull(goodsCategory.getCategoryRank())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = MallCategoryService.updateGoodsCategory(goodsCategory);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 详情
     */
    @shopmanAction(value = "查看商品分类详情")
    @GetMapping("/categories/info/{id}")
    @ResponseBody
    public Result info(@PathVariable("id") Long id) {
        GoodsCategory goodsCategory = MallCategoryService.getGoodsCategoryById(id);
        if (goodsCategory == null) {
            return ResultGenerator.genFailResult("未查询到数据");
        }
        return ResultGenerator.genSuccessResult(goodsCategory);
    }

    /**
     * 分类删除
     */
    @shopmanAction(value = "删除商品分类")
    @RequestMapping(value = "/categories/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (MallCategoryService.deleteBatch(ids)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("删除失败");
        }
    }


}