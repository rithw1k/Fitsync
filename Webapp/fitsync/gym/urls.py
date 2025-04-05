from django.urls import path
from .views import *

urlpatterns = [
    path('', login_view, name='login'),
    path('dashboard/', dashboard_view, name='dashboard'),
    path('trainer_dashboard/', trainer_dashboard_view, name='trainer_dashboard'),
    path('logout/', logout_view, name='logout'),
    path('trainers/add/', add_trainer_view, name='add_trainer'),
    path('trainers/manage/', manage_trainers_view, name='manage_trainers'),
    path('products/add/', add_product_view, name='add_product'),
    path('products/manage/', manage_products_view, name='manage_products'),
    path('orders/', orders_view, name='orders'),
    path('users/', users_view, name='users'),
    path('bookings/', view_bookings, name='view_bookings'),
    path('daily_food/', daily_food_view, name='daily_food'),
    path('upload_video/', upload_video_view, name='upload_video'),
    path('manage_videos/', manage_videos_view, name='manage_videos'),
    path('reviews/', review_admin_view, name='review_admin'),
    path('trainer_reviews/', review_trainer_view, name='review_trainer'),
    # New API endpoints
    path('api/signup/', api_signup_view, name='api_signup'),
    path('api/login/', api_login_view, name='api_login'),
    path('api/products/', api_products_view, name='api_products'),
    path('api/add-to-cart/', api_add_to_cart_view, name='api_add_to_cart'),
    path('api/cart/', api_cart_view, name='api_cart'),
    path('api/update-cart/', api_update_cart_view, name='api_update_cart'),
    path('api/create-order/', api_create_order_view, name='api_create_order'),
    path('api/orders/', api_orders_view, name='api_orders'),
    path('api/add-food-log/', api_add_food_log, name='api_add_food_log'),
    path('api/food-history/', api_food_history, name='api_food_history'),
    path('api/get-trainers/', api_get_trainers, name='api_get_trainers'),
    path('api/create-booking/', api_create_booking, name='api_create_booking'),
    path('api/check-booking/', api_check_booking, name='api_check_booking'),
    path('api/delete-booking/', api_delete_booking, name='api_delete_booking'),
    path('api/get-trainer-videos/', api_get_trainer_videos, name='api_get_trainer_videos'),
    path('api/get-review/', api_get_review, name='api_get_review'),  # New endpoint
    path('api/create-review/', api_create_review, name='api_create_review'),  # New endpoint
    path('api/update-review/', api_update_review, name='api_update_review'),
]