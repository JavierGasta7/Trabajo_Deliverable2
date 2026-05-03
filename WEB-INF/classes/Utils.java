import javax.servlet.http.HttpSession;

public class Utils {
    public static String header(String title, HttpSession session) {
        // Determinamos a dónde enviar al usuario según su rol
        String homeLink = "index.html"; // Por defecto al index
        
        if (session != null && session.getAttribute("userRole") != null) {
            String role = (String) session.getAttribute("userRole");
            
            if (role.equalsIgnoreCase("admin")) {
                homeLink = "AdminDashboardServlet";
            } else if (role.equalsIgnoreCase("instructor") || role.equalsIgnoreCase("manager")) {
                homeLink = "InstructorDashboardServlet";
            } else if (role.equalsIgnoreCase("member")) {
                homeLink = "MemberDashboardServlet";
            }
        }

        StringBuilder str = new StringBuilder();
        str.append("<!DOCTYPE HTML><html><head>");
        str.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
        str.append("<title>" + title + "</title>");
        str.append("<link rel='stylesheet' href='style.css?v=" + System.currentTimeMillis() + "'>");
        str.append("<script>");
        str.append("function confirmarBorrar(msg){return confirm(msg||'¿Seguro que quieres realizar esta acción?');}");
        str.append("function validarSignUp(f){if(f.password.value!==f.password2.value){alert('Las contraseñas no coinciden');return false;}if(f.password.value.length<6){alert('La contraseña debe tener al menos 6 caracteres');return false;}if(!/^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$/.test(f.email.value)){alert('Email no válido');return false;}return true;}");
        str.append("function validarEquipo(f){if(!f.name.value.trim()){alert('El nombre es obligatorio');return false;}if(f.notes&&f.notes.value.length>200){alert('Las notas no pueden superar 200 caracteres');return false;}return true;}");
        str.append("function contarNotas(input,spanId){var s=document.getElementById(spanId);if(s)s.textContent=input.value.length+'/200';}");
        str.append("function avisoEstadoBroken(sel){if(sel.value==='broken'){alert('Marcado como roto: aparecerá como crítico en Alertas de Mantenimiento');}}");
        str.append("function mostrarPassword(id){var i=document.getElementById(id);if(!i)return;i.type=i.type==='password'?'text':'password';}");
        str.append("function filtrarTabla(idInput,idTabla){var q=document.getElementById(idInput).value.toLowerCase();var t=document.getElementById(idTabla);if(!t)return;var r=t.getElementsByTagName('tr');for(var i=1;i<r.length;i++){r[i].style.display=r[i].innerText.toLowerCase().indexOf(q)>=0?'':'none';}}");
        str.append("function marcarReparadoHoy(){var s=document.getElementsByName('status')[0];var d=document.getElementsByName('lastMaintenance')[0];if(s)s.value='available';if(d){var t=new Date();d.value=t.getFullYear()+'-'+String(t.getMonth()+1).padStart(2,'0')+'-'+String(t.getDate()).padStart(2,'0');}}");
        str.append("function contarComentario(input,spanId){var s=document.getElementById(spanId);if(s)s.textContent=input.value.length+'/500';}");
        str.append("function medidorPassword(input,divId){var d=document.getElementById(divId);if(!d)return;var v=input.value;var s=0;if(v.length>=6)s++;if(v.length>=10)s++;if(/[A-Z]/.test(v)&&/[a-z]/.test(v))s++;if(/[0-9]/.test(v))s++;if(/[^A-Za-z0-9]/.test(v))s++;var col=['#e5e7eb','#ef4444','#f59e0b','#eab308','#10b981','#059669'];var lab=['','Muy d&eacute;bil','D&eacute;bil','Media','Fuerte','Muy fuerte'];var w=(s*20)+'%';d.innerHTML=\"<div style='background:#e5e7eb; height:6px; border-radius:3px; overflow:hidden;'><div style='height:6px; width:\"+w+\"; background:\"+col[s]+\";'></div></div><div style='font-size:11px; color:\"+col[s]+\"; margin-top:2px;'>\"+lab[s]+\"</div>\";}");
        str.append("</script>");
        str.append("</head><body>");
        
        str.append("<div class='menu'>");
        str.append("<span class='brand'>GYMTRACK</span>");
        

        str.append("<a href='" + homeLink + "'>Inicio</a>"); 
        str.append("<a href='LogoutServlet'>Cerrar Sesi\u00f3n</a>");
        str.append("</div>");
        
        str.append("<div class='subheader'>" + title + "</div>");
        return str.toString();
    }

    // Mantén el método footer igual
    public static String footer(String title) {
        return "</body></html>";
    }

    // Formatea fechas de Access: "yyyy-MM-dd ..." -> "yyyy-MM-dd"
    public static String formatDate(String s) {
        if (s == null) return "";
        if (s.length() >= 10) return s.substring(0, 10);
        return s;
    }

    // Formatea horas de Access. Valores t\u00edpicos:
    //   "1899-12-30 10:00:00.0"  -> "10:00"
    //   "10:00:00"                -> "10:00"
    //   "10:00"                   -> "10:00"
    public static String formatTime(String s) {
        if (s == null) return "";
        int colonIdx = s.indexOf(':');
        if (colonIdx <= 0) return s;
        int start = colonIdx - 2;
        if (start < 0) start = 0;
        int end = start + 5;
        if (end > s.length()) end = s.length();
        return s.substring(start, end);
    }
}