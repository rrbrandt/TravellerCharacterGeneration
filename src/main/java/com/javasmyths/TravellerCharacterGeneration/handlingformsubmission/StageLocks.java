package com.javasmyths.TravellerCharacterGeneration.handlingformsubmission;

import javax.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
import com.javasmyths.TravellerCharacterGeneration.model.CareerProgress;

/** The session owns completed stages; revisiting a tab never reopens it. */
@Configuration
public class StageLocks implements WebMvcConfigurer {
  public static int active(HttpSession session) {
    Integer stage = (Integer) session.getAttribute("activeStage");
    return stage == null ? 0 : stage;
  }
  public static int stage(String view) {
    if (view == null) return -1;
    switch (view) {
      case "newcharacter": return 0;
      case "characteristics": return 1;
      case "selectservice": return 2;
      case "selectskills": return 3;
      case "musterout": return 4;
      case "finalcharacter": return 5;
      default: return -1;
    }
  }
  @Override public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new HandlerInterceptor() {
      @Override public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (!"POST".equals(req.getMethod())) return true;
        String path = req.getServletPath();
        int owner = stage(path.length() > 0 ? path.substring(1) : "");
        if (owner >= 0 && owner < active(req.getSession())) {
          res.sendRedirect(req.getContextPath() + "/stage?stage=" + owner);
          return false;
        }
        return true;
      }
      @Override public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView mv) {
        if (mv == null) return;
        int shown = stage(mv.getViewName());
        if (shown < 0) return;
        HttpSession session = req.getSession();
        CareerProgress career = (CareerProgress) session.getAttribute("careerProgress");
        boolean ready = shown < 3 || (shown == 3 && career != null && career.getService() != null)
            || (shown == 4 && career != null && career.isMusteredOut())
            || (shown == 5 && career != null && career.isGenerationComplete());
        int active = active(session);
        if (ready && shown > active) { active = shown; session.setAttribute("activeStage", active); }
        mv.addObject("activeStage", active);
        mv.addObject("readOnlyStage", shown < active);
        if ("selectservice".equals(mv.getViewName())) {
          boolean enlisted = career != null && career.getService() != null;
          mv.addObject("acceptButton", "hidden");
          mv.addObject("attemptButton", enlisted ? "hidden" : "display");
          mv.addObject("continueButton", enlisted ? "display" : "hidden");
        }
        res.setHeader("Cache-Control", "no-store");
      }
    });
  }
}
