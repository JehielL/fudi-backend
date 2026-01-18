package com.BiteBooking.backend;


import com.BiteBooking.backend.model.*;
import com.BiteBooking.backend.repository.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Main {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Main.class, args);

		UserRepository userRepository = context.getBean(UserRepository.class);
		
		if (userRepository.count() > 0) {
			System.out.println(">>> Base de datos ya tiene datos, saltando inicialización...");
			return;
		}
		
		System.out.println(">>> Base de datos vacía, insertando datos de prueba...");
		
		RestaurantRepository restaurantRepository = context.getBean(RestaurantRepository.class);
		MenuRepository menuRepository = context.getBean(MenuRepository.class);
		DishRepository dishRepository = context.getBean(DishRepository.class);
		BookingRepository bookingRepository = context.getBean(BookingRepository.class);
		RatingRepository ratingRepository = context.getBean(RatingRepository.class);
		PromotionRepository promotionRepository = context.getBean(PromotionRepository.class);

		PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
		User user1 = User.builder()
				.firstName("Jehiel").lastName("Linarez")
				.birthdayDate(LocalDate.now())
				.email("jehiel@bitefood.es")
				.password(passwordEncoder.encode("admin1234"))
				.phone("123456789").role(Role.USER)
				.imgUser("https://i.ibb.co/gzMWwzK/profile.jpg")
				.city("Madrid").aboutMe("I love Food")
				.authProviders(new HashSet<>(Set.of(AuthProvider.LOCAL)))
				.build();
		User user2 = User.builder()
				.firstName("Carlos").lastName("Feliz")
				.birthdayDate(LocalDate.now())
				.email("carlos@bitefood.es")
				.password(passwordEncoder.encode("admin1234"))
				.phone("123456789").role(Role.ADMIN)
				.imgUser("https://i.ibb.co/kKh10Bg/1682683426487.jpg")
				.city("Madrid").aboutMe("Amante de la buena cocina")
				.authProviders(new HashSet<>(Set.of(AuthProvider.LOCAL)))
				.build();
		User user3 = User.builder()
				.firstName("Gandalf").lastName("The White")
				.birthdayDate(LocalDate.now())
				.email("gandalf@bitefood.es")
				.password(passwordEncoder.encode("admin1234"))
				.phone("639965587").role(Role.ADMIN)
				.imgUser("https://i.ibb.co/zF2sWWf/gandalf2.jpg")
				.city("Tierra Media").aboutMe("You shall not pass!")
				.authProviders(new HashSet<>(Set.of(AuthProvider.LOCAL)))
				.build();
		User user4 = User.builder()
				.firstName("Maria Laura").lastName("Asuaje Palmero")
				.birthdayDate(LocalDate.of(1991,5,10))
				.email("malauraasuaje@bitefood.es")
				.password(passwordEncoder.encode("admin1234"))
				.phone("622139560").role(Role.ADMIN)
				.imgUser("https://i.ibb.co/gzMWwzK/profile.jpg")
				.city("Madrid").aboutMe("The Cookies Queen")
				.authProviders(new HashSet<>(Set.of(AuthProvider.LOCAL)))
				.build();
		userRepository.saveAll(List.of(user1,user2, user3, user4));
		userRepository.saveAll(List.of(user1,user2, user3, user4));

		// Creación de restaurantes (mantener Oh-Toro, Maguro, SLVJ y añadir 5 top global)
		Restaurant ohToro = new Restaurant(null, "Oh Toro", "+81 3-5555-0101", RestaurantType.JAPANESE_FOOD,
				"Omakase intimista de sushi de temporada en Shibuya.", LocalTime.of(12, 0), LocalTime.of(23, 0), true,
				"https://i.ibb.co/JnNWhhp/oh-toro.jpg", "Tokio", "Shibuya Crossing 1-1", "B1", "150-0002", 4.8, null, user1);
		Restaurant maguro = new Restaurant(null, "Maguro", "+1 305-777-2244", RestaurantType.FUSION_FOOD,
				"Raw bar y robata japonesa con guiños americanos en Miami.", LocalTime.of(11, 30), LocalTime.of(23, 30), true,
				"https://images.otstatic.com/prod/25860960/1/large.jpg", "Miami", "Brickell Ave 495", "12", "33131", 4.6, null, user2);
		Restaurant slvj = new Restaurant(null, "SLVJ", "+34 915-555-222", RestaurantType.FUSION_FOOD,
				"Nikkei y coctelería de autor en Madrid.", LocalTime.of(13, 0), LocalTime.of(2, 0), true,
				"https://slvj.es/restaurantes/madridcanalejas.jpg", "Madrid", "Calle de Alcalá 21", "1", "28014", 4.5, null, user3);
		Restaurant central = new Restaurant(null, "Central", "+51 1 242-8515", RestaurantType.PERUVIAN_FOOD,
				"Experiencia de territorios peruanos por ecosistemas.", LocalTime.of(12, 30), LocalTime.of(22, 30), true,
				"https://res.cloudinary.com/rainforest-cruises/images/c_fill,g_auto/f_auto,q_auto/w_1120,h_732,c_fill,g_auto/v1625762943/Peruvian-Food-Main-Image/Peruvian-Food-Main-Image-1120x732.jpg",
				"Lima", "Av. Pedro de Osma 301", "Barranco", "15063", 4.9, null, user4);
		Restaurant noma = new Restaurant(null, "Noma", "+45 3296 3297", RestaurantType.FUSION_FOOD,
				"Temporadas nórdicas con fermentos y foraging en Copenhague.", LocalTime.of(12, 0), LocalTime.of(22, 0), true,
				"https://www.gastroeconomy.com/wp-content/uploads/2019/11/Gastroeconomy_Noma2019_10.jpg", "Copenhague", "Strandgade 93", "1", "1401", 4.9, null, user2);
		Restaurant emp = new Restaurant(null, "Eleven Madison Park", "+1 212-889-0905", RestaurantType.VEGAN_FOOD,
				"Degustación plant-based con enfoque sostenible en NYC.", LocalTime.of(17, 30), LocalTime.of(23, 30), true,
				"https://images.squarespace-cdn.com/content/v1/661d75b635d9930903bb9d39/84f6dc5f-969a-443c-a3a3-2b11882a3965/Eleven+Madison+Park+Dining+Room", "Nueva York", "11 Madison Ave", "", "10010", 4.8, null, user1);
		Restaurant disfrutar = new Restaurant(null, "Disfrutar", "+34 933-486-896", RestaurantType.FUSION_FOOD,
				"Vanguardia mediterránea en el Eixample de Barcelona.", LocalTime.of(13, 0), LocalTime.of(23, 0), true,
				"https://cloudfront-eu-central-1.images.arcpublishing.com/prisa/CNQKOSNOGZFLBKKTKSKUGBP6UI.jpg", "Barcelona", "Carrer de Villarroel 163", "", "08036", 4.9, null, user3);
		Restaurant gaggan = new Restaurant(null, "Gaggan Anand", "+66 2 652 1700", RestaurantType.INDIAN_FOOD,
				"Alta cocina india progresiva en Bangkok.", LocalTime.of(17, 0), LocalTime.of(23, 30), true,
				"https://images.hindustantimes.com/rf/image_size_630x354/HT/p2/2019/11/13/Pictures/_9ed20c22-0628-11ea-8da7-95ed4a38ab68.jpg", "Bangkok", "68/1 Soi Langsuan", "", "10330", 4.8, null, user4);

		restaurantRepository.saveAll(List.of(ohToro, maguro, slvj, central, noma, emp, disfrutar, gaggan));

		// Menús coherentes por restaurante
		Menu ohToroOmakase = new Menu(null, "Omakase Shun", "Nigiris y sashimi de temporada con pesca diaria de Tsukiji.",
				"https://www.onlyyouhotels.com/content/imgsxml/galerias/panel_galeriarestauracion/1/1836.jpg", true, RestaurantType.JAPANESE_FOOD, true, ohToro);
		Menu ohToroKaiseki = new Menu(null, "Kaiseki Seishin", "Secuencia kaiseki con wagyu A5 y tempuras ligeras.",
				"https://d28dpoj42hxr8c.cloudfront.net/files/user/201803091609_12.jpg?v=1520579395", true, RestaurantType.JAPANESE_FOOD, true, ohToro);

		Menu maguroRaw = new Menu(null, "Sushi Lab", "Sushi bar creativo con toques ahumados y salsas yuzu-miso.",
				"https://estaticos.esmadrid.com/cdn/farfuture/_Xjt0-7oPc_QhFzHnKMWUBGuG4z9AEpS2LsonR6wSHs/mtime:1646730092/sites/default/files/styles/content_type_full/public/recursosturisticos/restaurantes/jl1_1426675355.278.jpg?itok=AB5_23QG", true, RestaurantType.FUSION_FOOD, true, maguro);
		Menu maguroRobata = new Menu(null, "Robata Bites", "Yakitori, robata de mar y baos glaseados.",
				"https://eatandlovemadrid.es/files/wordpress/wp-content/uploads/2019/05/nunca-cocines.jpg", true, RestaurantType.FUSION_FOOD, true, maguro);

		Menu slvjNikkei = new Menu(null, "Nikkei Nights", "Ceviches, tiraditos y sushi caliente con ají amarillo.",
				"https://madriddiferente.com/wp-content/uploads/2022/01/SANTOS-Y-DESAMPARADOS-coctel-Dragon-amarillo.jpg", true, RestaurantType.FUSION_FOOD, true, slvj);
		Menu slvjBar = new Menu(null, "Jungle Drinks", "Coctelería de autor y bar bites para compartir.",
				"https://www.7canibales.com/wp-content/uploads/sites/2/2022/06/Nigiri-de-bogavante-1-400x296.jpg", true, RestaurantType.BAR, true, slvj);

		Menu centralEcosistemas = new Menu(null, "Mater Ecosystems", "Recorrido por costa, Andes y Amazonía con insumos nativos.",
				"https://res.cloudinary.com/rainforest-cruises/images/c_fill,g_auto/f_auto,q_auto/w_1120,h_732,c_fill,g_auto/v1625762943/Peruvian-Food-Main-Image/Peruvian-Food-Main-Image-1120x732.jpg", true, RestaurantType.PERUVIAN_FOOD, true, central);
		Menu centralMar = new Menu(null, "Mar & Andes", "Maridaje de mariscos peruanos con tubérculos andinos.",
				"https://media.istockphoto.com/id/1414641728/photo/tacos-al-pastor-from-mexico.jpg", true, RestaurantType.PERUVIAN_FOOD, true, central);

		Menu nomaNordic = new Menu(null, "Nordic Seasons", "Verduras, mariscos y fermentos según temporada nórdica.",
				"https://i.ibb.co/VB8Lx0b/noma-exterior.jpg", true, RestaurantType.FUSION_FOOD, true, noma);

		Menu empPlant = new Menu(null, "Plant-Based Tasting", "Degustación vegetal con técnicas clásicas y fondos intensos.",
				"https://i.ibb.co/5kCqHqZ/eleven-madison-park.jpg", true, RestaurantType.VEGAN_FOOD, true, emp);

		Menu disfrutarMed = new Menu(null, "Mediterranean Avant", "Platos lúdicos, sferificaciones y mar mediterráneo.",
				"https://offloadmedia.feverup.com/madridsecreto.co/wp-content/uploads/2020/02/20104952/mejores-brunches-madrid-marieta.jpg", true, RestaurantType.FUSION_FOOD, true, disfrutar);

		Menu gagganProg = new Menu(null, "Progressive India", "Snacks de 25 bocados, street food elevado y curries reinventados.",
				"https://i.blogs.es/0d79fc/captura-de-pantalla-2019-10-18-a-la-s-13.03.51/650_1200.png", true, RestaurantType.INDIAN_FOOD, true, gaggan);

		menuRepository.saveAll(List.of(
				ohToroOmakase, ohToroKaiseki, maguroRaw, maguroRobata, slvjNikkei, slvjBar,
				centralEcosistemas, centralMar, nomaNordic, empPlant, disfrutarMed, gagganProg
		));

		// Platos de ejemplo por menú
		Dish toroNigiri = new Dish(null, "Chu-Toro Nigiri", "Atún graso con shari avinagrado y wasabi fresco.", 18.0, "https://i.ibb.co/gzMWwzK/profile.jpg", true, false, ohToroOmakase);
		Dish wagyuNigiri = new Dish(null, "Wagyu A5 Nigiri", "Lomo A5 flambeado con salsa ponzu envejecida.", 24.0, "https://i.ibb.co/kKh10Bg/1682683426487.jpg", true, false, ohToroKaiseki);
		Dish matchaCheesecake = new Dish(null, "Matcha Cheesecake", "Cheesecake ligero con crumble de sésamo negro.", 9.5, "https://i.ibb.co/zF2sWWf/gandalf2.jpg", true, true, ohToroKaiseki);

		Dish spicyTuna = new Dish(null, "Spicy Tuna Crispy Rice", "Arroz crujiente con tartar de atún, jalapeño y yuzu.", 14.0, "https://estaticos.esmadrid.com/cdn/farfuture/_Xjt0-7oPc_QhFzHnKMWUBGuG4z9AEpS2LsonR6wSHs/mtime:1646730092/sites/default/files/styles/content_type_full/public/recursosturisticos/restaurantes/jl1_1426675355.278.jpg?itok=AB5_23QG", true, true, maguroRaw);
		Dish robataCorn = new Dish(null, "Maíz Robata", "Maíz dulce a la robata, mantequilla miso y shichimi.", 11.0, "https://eatandlovemadrid.es/files/wordpress/wp-content/uploads/2019/05/nunca-cocines.jpg", true, false, maguroRobata);
		Dish wagyuBao = new Dish(null, "Bao de Wagyu", "Bao esponjoso con brisket glaseado y pepino encurtido.", 13.0, "https://madriddiferente.com/wp-content/uploads/2022/01/SANTOS-Y-DESAMPARADOS-coctel-Dragon-amarillo.jpg", true, false, maguroRobata);

		Dish cevicheNikkei = new Dish(null, "Ceviche Nikkei", "Corvina, leche de tigre de ají amarillo y furikake.", 16.0, "https://www.7canibales.com/wp-content/uploads/sites/2/2022/06/Nigiri-de-bogavante-1-400x296.jpg", true, false, slvjNikkei);
		Dish sushiCaliente = new Dish(null, "Sushi Caliente de Salmón", "Salmón sellado, crema togarashi y cebollín.", 15.0, "https://www.onlyyouhotels.com/content/imgsxml/galerias/panel_galeriarestauracion/1/1836.jpg", true, true, slvjNikkei);
		Dish jungleBite = new Dish(null, "Jungle Gyoza", "Gyozas de pato con salsa de maracuyá y lima kaffir.", 12.0, "https://res.cloudinary.com/rainforest-cruises/images/c_fill,g_auto/f_auto,q_auto/w_1120,h_732,c_fill,g_auto/v1625762943/Peruvian-Food-Main-Image/Peruvian-Food-Main-Image-1120x732.jpg", true, false, slvjBar);

		Dish tuberAndes = new Dish(null, "Tubérculos Andinos", "Mashua, oca y papalisa con emulsión de huacatay.", 17.0, "https://media.istockphoto.com/id/1414641728/photo/tacos-al-pastor-from-mexico.jpg", true, false, centralEcosistemas);
		Dish tiradito = new Dish(null, "Tiradito de Conchas", "Conchas de abanico, ají limo y aceite de oliva chacra.", 18.0, "https://res.cloudinary.com/rainforest-cruises/images/c_fill,g_auto/f_auto,q_auto/w_1120,h_732,c_fill,g_auto/v1625762943/Peruvian-Food-Main-Image/Peruvian-Food-Main-Image-1120x732.jpg", true, false, centralMar);
		Dish cacaoAmazonia = new Dish(null, "Cacao Amazonía", "Texturas de cacao, sal de maras y frutos rojos amazónicos.", 10.0, "https://offloadmedia.feverup.com/madridsecreto.co/wp-content/uploads/2020/02/20104952/mejores-brunches-madrid-marieta.jpg", true, true, centralEcosistemas);

		Dish fermentos = new Dish(null, "Verduras Fermentadas", "Raíces fermentadas, mantequilla tostada y brotes nórdicos.", 19.0, "https://upload.wikimedia.org/wikipedia/commons/2/2f/Noma_Copenhagen.jpg", true, true, nomaNordic);
		Dish cangrejo = new Dish(null, "Cangrejo Real", "Cangrejo real, caldo ahumado de pino y eneldo.", 24.0, "https://i.ibb.co/5kCqHqZ/eleven-madison-park.jpg", true, false, nomaNordic);

		Dish beetWellington = new Dish(null, "Beet Wellington", "Remolacha en hojaldre, salsa de trufa negra y jus vegetal.", 22.0, "https://i.ibb.co/kKh10Bg/1682683426487.jpg", true, true, empPlant);
		Dish sunflower = new Dish(null, "Girasol Confitado", "Girasol, miso de semillas y salsa de cítricos.", 18.0, "https://i.ibb.co/zF2sWWf/gandalf2.jpg", true, true, empPlant);

		Dish aceitunas = new Dish(null, "Aceitunas Sferificadas", "Clásico esferificado con explosión de aceituna verde.", 9.0, "https://estaticos.esmadrid.com/cdn/farfuture/_Xjt0-7oPc_QhFzHnKMWUBGuG4z9AEpS2LsonR6wSHs/mtime:1646730092/sites/default/files/styles/content_type_full/public/recursosturisticos/restaurantes/jl1_1426675355.278.jpg?itok=AB5_23QG", true, false, disfrutarMed);
		Dish coral = new Dish(null, "Coral de Gamba Roja", "Gamba roja de Palamós, coral y aire de mar.", 21.0, "https://madriddiferente.com/wp-content/uploads/2022/01/SANTOS-Y-DESAMPARADOS-coctel-Dragon-amarillo.jpg", true, false, disfrutarMed);

		Dish yogurtExplosion = new Dish(null, "Yogurt Explosion", "Bocado icónico de yogur, especias y nitrógeno.", 8.0, "https://www.7canibales.com/wp-content/uploads/sites/2/2022/06/Nigiri-de-bogavante-1-400x296.jpg", true, true, gagganProg);
		Dish curryLeaf = new Dish(null, "Curry Leaf Prawn", "Langostino, curry leaf y coco ahumado.", 16.0, "https://res.cloudinary.com/rainforest-cruises/images/c_fill,g_auto/f_auto,q_auto/w_1120,h_732,c_fill,g_auto/v1625762943/Peruvian-Food-Main-Image/Peruvian-Food-Main-Image-1120x732.jpg", true, false, gagganProg);

		dishRepository.saveAll(List.of(
				toroNigiri, wagyuNigiri, matchaCheesecake,
				spicyTuna, robataCorn, wagyuBao,
				cevicheNikkei, sushiCaliente, jungleBite,
				tuberAndes, tiradito, cacaoAmazonia,
				fermentos, cangrejo,
				beetWellington, sunflower,
				aceitunas, coral,
				yogurtExplosion, curryLeaf
		));

		// Ratings resumidos y coherentes
		Rating r1 = new Rating(null, 5, "Nigiri impecable, arroz perfecto.", ohToroOmakase, user1, null);
		Rating r2 = new Rating(null, 5, "Experiencia kaiseki redonda.", ohToroKaiseki, user2, null);
		Rating r3 = new Rating(null, 4, "Great crispy rice, buen picante.", maguroRaw, user3, null);
		Rating r4 = new Rating(null, 4, "Robata de wagyu muy jugosa.", maguroRobata, user4, null);
		Rating r5 = new Rating(null, 5, "Ceviche nikkei top y cócteles creativos.", slvjNikkei, user1, null);
		Rating r6 = new Rating(null, 4, "Bar bites para compartir, buen ambiente.", slvjBar, user2, null);
		Rating r7 = new Rating(null, 5, "Mater Ecosystems es un viaje por Perú.", centralEcosistemas, user3, null);
		Rating r8 = new Rating(null, 5, "Mar & Andes: conchas y ají perfectos.", centralMar, user4, null);
		Rating r9 = new Rating(null, 5, "Fermentos y mariscos brutales.", nomaNordic, user2, null);
		Rating r10 = new Rating(null, 5, "Plant-based fine dining sorprendente.", empPlant, user1, null);
		Rating r11 = new Rating(null, 5, "Coral de gamba y esferificaciones icónicas.", disfrutarMed, user3, null);
		Rating r12 = new Rating(null, 5, "Yogurt explosion sigue siendo magia.", gagganProg, user4, null);
		ratingRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12));

		// =====================================================
		// RESERVAS DE PRUEBA - DATOS REALISTAS
		// =====================================================
		
		// Fechas para las reservas
		LocalDate hoy = LocalDate.now();
		LocalDate manana = hoy.plusDays(1);
		LocalDate pasadoManana = hoy.plusDays(2);
		LocalDate enUnaSemana = hoy.plusDays(7);
		LocalDate ayer = hoy.minusDays(1);
		LocalDate haceDias = hoy.minusDays(3);
		
		// Reservas PENDIENTES (para hoy y futuro)
		bookingRepository.save(new Booking(null, hoy, LocalTime.of(13, 30), user1, 2,
			"Mesa junto a la barra de sushi", BookingStatus.PENDING, true, 5, ohToro, "Cumpleaños"));
		bookingRepository.save(new Booking(null, hoy, LocalTime.of(20, 0), user2, 4,
			"Alergia a frutos secos", BookingStatus.PENDING, false, 12, maguro, ""));
		bookingRepository.save(new Booking(null, manana, LocalTime.of(14, 0), user3, 6,
			"Reunión de negocios, sala semi privada", BookingStatus.PENDING, true, 8, emp, "Vino tinto"));
		bookingRepository.save(new Booking(null, manana, LocalTime.of(21, 30), user4, 2,
			"Cena romántica, menú degustación", BookingStatus.PENDING, true, 3, disfrutar, "Flores en mesa"));
		bookingRepository.save(new Booking(null, pasadoManana, LocalTime.of(13, 0), user1, 8,
			"Grupo familiar con niños", BookingStatus.PENDING, false, 15, central, "Trona"));

		// Reservas CONFIRMADAS
		bookingRepository.save(new Booking(null, hoy, LocalTime.of(14, 30), user2, 3,
			"Cliente VIP - servicio premium", BookingStatus.CONFIRMED, true, 1, ohToro, "Menu degustacion"));
		bookingRepository.save(new Booking(null, hoy, LocalTime.of(21, 0), user3, 5,
			"Celebración de ascenso", BookingStatus.CONFIRMED, true, 10, noma, "Champagne"));
		bookingRepository.save(new Booking(null, manana, LocalTime.of(20, 30), user1, 4,
			"", BookingStatus.CONFIRMED, false, 7, slvj, ""));
		bookingRepository.save(new Booking(null, enUnaSemana, LocalTime.of(14, 0), user4, 12,
			"Comida de empresa", BookingStatus.CONFIRMED, true, 20, gaggan, "Menu ejecutivo"));

		bookingRepository.save(new Booking(null, ayer, LocalTime.of(13, 0), user1, 2,
			"Todo perfecto", BookingStatus.COMPLETED, true, 4, ohToro, ""));
		bookingRepository.save(new Booking(null, ayer, LocalTime.of(20, 30), user2, 6,
			"Excelente servicio", BookingStatus.COMPLETED, false, 11, maguro, ""));
		bookingRepository.save(new Booking(null, haceDias, LocalTime.of(21, 0), user3, 4,
			"Muy buena comida", BookingStatus.COMPLETED, true, 6, slvj, ""));
		bookingRepository.save(new Booking(null, haceDias, LocalTime.of(14, 30), user4, 3,
			"", BookingStatus.COMPLETED, true, 9, central, "Postre especial"));
		bookingRepository.save(new Booking(null, haceDias, LocalTime.of(13, 30), user1, 2,
			"Recomendaré este lugar", BookingStatus.COMPLETED, false, 2, disfrutar, ""));

		bookingRepository.save(new Booking(null, hoy, LocalTime.of(19, 0), user2, 4,
			"Cancelado por enfermedad", BookingStatus.CANCELLED, true, 14, ohToro, ""));
		bookingRepository.save(new Booking(null, manana, LocalTime.of(21, 0), user4, 2,
			"Cambio de planes de último momento", BookingStatus.CANCELLED, false, 5, emp, ""));

		bookingRepository.save(new Booking(null, ayer, LocalTime.of(20, 0), user3, 3,
			"Cliente no llegó sin avisar", BookingStatus.NO_SHOW, true, 8, maguro, ""));
		bookingRepository.save(new Booking(null, haceDias, LocalTime.of(14, 0), user1, 5,
			"No contestaron llamadas", BookingStatus.NO_SHOW, false, 13, gaggan, ""));

		bookingRepository.save(new Booking(null, pasadoManana, LocalTime.of(22, 0), user2, 15,
			"Grupo demasiado grande para nuestra capacidad", BookingStatus.REJECTED, true, 0, ohToro, ""));

		bookingRepository.save(new Booking(null, enUnaSemana, LocalTime.of(13, 0), user1, 2,
			"Aniversario de boda", BookingStatus.PENDING, true, 1, disfrutar, "Tarta sorpresa"));
		bookingRepository.save(new Booking(null, enUnaSemana, LocalTime.of(20, 0), user3, 6,
			"Despedida de soltero/a", BookingStatus.CONFIRMED, false, 16, slvj, "Decoracion"));
		bookingRepository.save(new Booking(null, hoy, LocalTime.of(12, 0), user4, 3,
			"Almuerzo rápido de trabajo", BookingStatus.CONFIRMED, true, 4, central, ""));
		bookingRepository.save(new Booking(null, pasadoManana, LocalTime.of(19, 30), user2, 4,
			"Primera cita - mesa tranquila", BookingStatus.PENDING, true, 2, noma, ""));


		// =====================================================
		// PROMOCIONES - DATOS DEMO POR RESTAURANTE
		// =====================================================
		
		// Promociones Oh Toro (Japanese Food - Alta gama)
		Promotion promoOhToroHappyHour = new Promotion(
			null,
			"Happy Hour Sake Premium",
			"20% descuento en sake premium y cerveza japonesa de 18:00 a 20:00",
			PromotionType.HAPPY_HOUR,
			new BigDecimal("20"),
			null,
			LocalDate.now().minusDays(5),
			LocalDate.now().plusMonths(2),
			LocalTime.of(18, 0),
			LocalTime.of(20, 0),
			"1,2,3,4,5",
			2,
			null,
			0,
			null,
			true,
			false,
			"https://images.unsplash.com/photo-1545569341-9eb8b30979d9?w=800",
			null,
			null,
			ohToro
		);
		
		Promotion promoOhToroSpecial = new Promotion(
			null,
			"Experiencia Omakase Especial",
			"Menú omakase exclusivo del chef a precio fijo para celebraciones",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("95.00"),
			LocalDate.now(),
			LocalDate.now().plusMonths(3),
			null,
			null,
			null,
			2,
			50,
			12,
			"OMAKASE2026",
			true,
			true,
			"https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=800",
			null,
			null,
			ohToro
		);

		// Promociones Maguro (Fusion - Miami vibes)
		Promotion promoMaguroHappyHour = new Promotion(
			null,
			"Sunset Happy Hour",
			"2x1 en cócteles signature y 30% off en robata bar",
			PromotionType.HAPPY_HOUR,
			new BigDecimal("30"),
			null,
			LocalDate.now().minusDays(10),
			LocalDate.now().plusMonths(1),
			LocalTime.of(17, 0),
			LocalTime.of(19, 30),
			"1,2,3,4,5",
			null,
			null,
			0,
			null,
			true,
			true,
			"https://images.unsplash.com/photo-1514361892635-6b07e31e75f9?w=800",
			null,
			null,
			maguro
		);
		
		Promotion promoMaguro2x1 = new Promotion(
			null,
			"2x1 Rolls Especiales",
			"Lleva 2 rolls especiales por el precio de 1 todos los martes",
			PromotionType.TWO_FOR_ONE,
			null,
			null,
			LocalDate.now(),
			LocalDate.now().plusMonths(2),
			null,
			null,
			"2",
			2,
			null,
			0,
			"ROLLDAY",
			true,
			false,
			"https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=800",
			null,
			null,
			maguro
		);

		// Promociones SLVJ (Nikkei & Cocktails - Madrid)
		Promotion promoSlvjFirstBooking = new Promotion(
			null,
			"Bienvenida SLVJ",
			"15% de descuento en tu primera reserva + welcome drink de cortesía",
			PromotionType.FIRST_BOOKING,
			new BigDecimal("15"),
			null,
			LocalDate.now().minusDays(20),
			LocalDate.now().plusMonths(6),
			null,
			null,
			null,
			1,
			200,
			45,
			"WELCOME15",
			true,
			true,
			"https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800",
			null,
			null,
			slvj
		);
		
		Promotion promoSlvjWeekend = new Promotion(
			null,
			"Jungle Weekend",
			"Menú degustación nikkei + 2 cócteles signature a precio especial",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("69.90"),
			LocalDate.now().plusDays(7),
			LocalDate.now().plusMonths(2),
			null,
			null,
			"6,7",
			2,
			100,
			0,
			null,
			true,
			false,
			"https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
			null,
			null,
			slvj
		);

		// Promociones Central (Peruvian - Lima)
		Promotion promoCentralTasting = new Promotion(
			null,
			"Mater Ecosystems Experience",
			"Experiencia completa de ecosistemas con maridaje incluido",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("180.00"),
			LocalDate.now(),
			LocalDate.now().plusMonths(4),
			null,
			null,
			null,
			2,
			80,
			23,
			"MATER2026",
			true,
			true,
			"https://images.unsplash.com/photo-1587573089168-09c7bef0b1a2?w=800",
			null,
			null,
			central
		);
		
		Promotion promoCentralLoyalty = new Promotion(
			null,
			"Programa Amigos de Central",
			"10% descuento permanente para clientes frecuentes (3+ visitas)",
			PromotionType.LOYALTY,
			new BigDecimal("10"),
			null,
			LocalDate.now().minusMonths(1),
			LocalDate.now().plusYears(1),
			null,
			null,
			null,
			null,
			null,
			0,
			"AMIGOSCENTRAL",
			true,
			false,
			"https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
			null,
			null,
			central
		);

		// Promociones Noma (Nordic - Copenhagen)
		Promotion promoNomaSeasons = new Promotion(
			null,
			"Nordic Seasons Tasting",
			"Menú de temporada con ingredientes de foraging local y fermentos",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("295.00"),
			LocalDate.now().plusDays(14),
			LocalDate.now().plusMonths(3),
			null,
			null,
			null,
			2,
			60,
			0,
			null,
			true,
			true,
			"https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800",
			null,
			null,
			noma
		);
		
		Promotion promoNomaLunch = new Promotion(
			null,
			"Almuerzo Nórdico",
			"Menú de almuerzo simplificado con esencia Noma a precio accesible",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("125.00"),
			LocalDate.now(),
			LocalDate.now().plusMonths(2),
			LocalTime.of(12, 0),
			LocalTime.of(15, 0),
			"1,2,3,4,5",
			2,
			null,
			0,
			null,
			true,
			false,
			"https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800",
			null,
			null,
			noma
		);

		// Promociones Eleven Madison Park (Plant-based - NYC)
		Promotion promoEmpPlantPioneer = new Promotion(
			null,
			"Plant-Based Pioneer",
			"20% descuento para nuevos comensales en experiencia plant-based",
			PromotionType.FIRST_BOOKING,
			new BigDecimal("20"),
			null,
			LocalDate.now().minusDays(7),
			LocalDate.now().plusMonths(3),
			null,
			null,
			null,
			2,
			150,
			67,
			"PLANTPIONEER",
			true,
			true,
			"https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
			null,
			null,
			emp
		);
		
		Promotion promoEmpSustainable = new Promotion(
			null,
			"Degustación Sostenible",
			"Menú degustación completo plant-based con vinos biodinámicos",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("335.00"),
			LocalDate.now(),
			LocalDate.now().plusMonths(5),
			null,
			null,
			null,
			1,
			40,
			8,
			null,
			true,
			false,
			"https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800",
			null,
			null,
			emp
		);

		// Promociones Disfrutar (Vanguardia - Barcelona)
		Promotion promoDisfrutarAvant = new Promotion(
			null,
			"Experiencia Vanguardista",
			"Menú degustación completo con maridaje de autor incluido",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("265.00"),
			LocalDate.now(),
			LocalDate.now().plusMonths(4),
			null,
			null,
			null,
			2,
			70,
			31,
			"VANGUARDIA",
			true,
			true,
			"https://images.unsplash.com/photo-1555244162-803834f70033?w=800",
			null,
			null,
			disfrutar
		);
		
		Promotion promoDisfrutarLunch = new Promotion(
			null,
			"Mediterranean Lunch",
			"Menú de mediodía con técnicas de vanguardia a precio especial",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("95.00"),
			LocalDate.now().minusDays(3),
			LocalDate.now().plusMonths(2),
			LocalTime.of(13, 0),
			LocalTime.of(16, 0),
			"2,3,4,5",
			2,
			null,
			0,
			null,
			true,
			false,
			"https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
			null,
			null,
			disfrutar
		);

		// Promociones Gaggan Anand (Indian Progressive - Bangkok)
		Promotion promoGaggan25Bites = new Promotion(
			null,
			"25 Bites Experience",
			"Experiencia completa de 25 bocados progresivos con maridaje",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("210.00"),
			LocalDate.now(),
			LocalDate.now().plusMonths(3),
			null,
			null,
			null,
			2,
			90,
			42,
			null,
			true,
			true,
			"https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800",
			null,
			null,
			gaggan
		);
		
		Promotion promoGagganStreetFood = new Promotion(
			null,
			"Street Food Elevated",
			"Menú street food reinventado - Viernes y Sábados noche",
			PromotionType.SPECIAL_MENU,
			null,
			new BigDecimal("85.00"),
			LocalDate.now().plusDays(5),
			LocalDate.now().plusMonths(2),
			LocalTime.of(19, 0),
			LocalTime.of(23, 0),
			"5,6",
			2,
			null,
			0,
			"STREETFOOD",
			true,
			false,
			"https://images.unsplash.com/photo-1596797038530-2c107229654b?w=800",
			null,
			null,
			gaggan
		);

		promotionRepository.saveAll(List.of(
			promoOhToroHappyHour, promoOhToroSpecial,
			promoMaguroHappyHour, promoMaguro2x1,
			promoSlvjFirstBooking, promoSlvjWeekend,
			promoCentralTasting, promoCentralLoyalty,
			promoNomaSeasons, promoNomaLunch,
			promoEmpPlantPioneer, promoEmpSustainable,
			promoDisfrutarAvant, promoDisfrutarLunch,
			promoGaggan25Bites, promoGagganStreetFood
		));

		System.out.println(">>> ✅ Datos de prueba insertados correctamente (incluye 16 promociones)");
	}
}